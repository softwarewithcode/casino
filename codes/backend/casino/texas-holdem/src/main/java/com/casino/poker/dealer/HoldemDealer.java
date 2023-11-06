package com.casino.poker.dealer;

import com.casino.common.cards.Deck;
import com.casino.common.dealer.TableGameCroupier;
import com.casino.common.dealer.PlayerTimingCroupier;
import com.casino.common.exception.IllegalPlayerCountException;
import com.casino.common.functions.Functions;
import com.casino.common.game.phase.GamePhaser;
import com.casino.common.message.Event;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.reload.DefaultReloader;
import com.casino.common.reload.Reload;
import com.casino.common.reload.Reloader;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.Seat;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.bet.BlindBetsHandler;
import com.casino.poker.bet.HoldemBlindBetsHandler;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.game.PokerData;
import com.casino.poker.message.PokerMapper;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.pot.PokerPotHandler;
import com.casino.poker.pot.Pot;
import com.casino.poker.pot.PotHandler;
import com.casino.poker.round.NewRoundTask;
import com.casino.poker.round.PokerRound;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import com.casino.poker.round.positions.HoldemRoundPlayers;
import com.casino.poker.showdown.ShowdownHandler;
import com.casino.poker.table.HoldemTable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main controller of Texas No-Limit Holdem game.
 */
public class HoldemDealer extends TableGameCroupier implements PokerDealer, GamePhaser, PlayerTimingCroupier {
    public static final BigDecimal GLOBAL_RESTRICTION_OF_MAXIMUM_RAISE = new BigDecimal("100000000000");
    private static final Logger LOGGER = Logger.getLogger(HoldemDealer.class.getName());
    private final HoldemTable table;
    private final PokerData pokerInitData;
    private final Reloader reloader;
    private final BlindBetsHandler blindsHandler;
    private final ShowdownHandler showdownHandler;
    private final PotHandler potHandler;
    private Deck deck;
    private boolean showdown;

    public HoldemDealer(HoldemTable holdemTable, PokerData pokerInitData) {
        super(holdemTable);
        this.table = holdemTable;
        this.deck = Deck.createAndShuffle();
        this.pokerInitData = pokerInitData;
        this.reloader = new DefaultReloader();
        this.potHandler = new PokerPotHandler(holdemTable, getGameData().rakePercent(), getGameData().rakeCap());
        this.blindsHandler = new HoldemBlindBetsHandler(potHandler);
        this.showdownHandler = new ShowdownHandler();
    }

    public Deck getDeck() {
        return deck;
    }

    @Override
    public <T extends CasinoPlayer> void onPlayerArrival(T player) {
        try {
            player.setStatus(PlayerStatus.NEW);
            voice.notifyPlayerArrival(player);
            croupierLock.lock();
            startTableIfPossible();
        } finally {
            croupierLock.unlock();
        }
    }

	private void startTableIfPossible() {
		if (canStartGame())
		    startGame();
	}

    @Override
    public void onRoundStart() {
        deck = Deck.createAndShuffle();
        try {
            table.getLock().lock();
            tryStartNewPokerRound();
            sendHoleCardsToPlayers();
        } catch (IllegalPlayerCountException i) {
            LOGGER.log(Level.INFO, "Not enough players to start round", i);
            changeToWaitingPlayersMode();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during preFlop preparation. Closing down table:", e);
            onError();
        } finally {
            table.getLock().unlock();
        }
    }

    private void changeToWaitingPlayersMode() {
        table.stopTiming();
        potHandler.clearPots();
        table.setStatus(TableStatus.WAITING_PLAYERS);
        table.getPlayers().forEach(CasinoPlayer::prepareForNextRound);
        if (table.getRound() != null)
            table.getRound().clearTableCards();
        sendStatusUpdate(Event.NO_BETS_NO_DEAL);
        table.updateCounterTime(0);
    }

    @Override
    public BlindBetsHandler getBlindsHandler() {
        return this.blindsHandler;
    }

    @Override
    public void onPlayerTimeout(CasinoPlayer timedOutPlayer) {
        LOGGER.entering(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer + " holdemTable:" + table.getId());
        try {
            table.getLock().lock();
            PokerPlayer holdemPlayer = (PokerPlayer) timedOutPlayer;
            holdemPlayer.sitOut(true);
            handleTimedOutOrLeavingPlayer(holdemPlayer);
            if (table.getGamePhase() != HoldemPhase.ROUND_COMPLETED)
                changePlayerOrPhase((PokerPlayer) timedOutPlayer);
        } finally {
            table.getLock().unlock();
            LOGGER.exiting(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer);
        }
    }

    @Override
    public <T extends CasinoPlayer> void onPlayerLeave(T leavingPlayer) {
        leavingPlayer.setStatus(PlayerStatus.LEFT);
        handleTimedOutOrLeavingPlayer((PokerPlayer) leavingPlayer);
//        if (table.isActivePlayer(leavingPlayer))  Autoplay calls changePlayerOrPhase
//            changePlayerOrPhase((PokerPlayer) leavingPlayer);
        // For HU situation !?
        if (table.getStatus() != TableStatus.RUNNING)
            table.sanitizeSeat(((PokerPlayer) leavingPlayer).getSeatNumber());
    }

    private void completeRound() {
        table.clearActivePlayer();
        if (table.getGamePhase() == HoldemPhase.RIVER)
            assignPokerHands();
        List<Pot> completedPots = potHandler.completePots();
        payForWinners(completedPots);
        giveBackExtraChipsFromTable();
        table.getRound().complete();
        removePlayersWhoLeft();
        handleReloads();
        table.updateGamePhase(HoldemPhase.ROUND_COMPLETED);
        voice.notifyEverybody(Event.ROUND_COMPLETED, null);
        updateSitoutPlayers();
        startNewRoundTimer();
    }

    private void removePlayersWhoLeft() {
        table.sanitizeSeatsByPlayerStatus(List.of(PlayerStatus.LEFT));
    }

    private void handleReloads() {
        if (reloader.hasPendingReloads())
            reloader.completePendingReloads();
    }

    @Override
    public void onError() {
        PlayerTimingCroupier.super.onError();
        reloader.completePendingReloads();
    }
    
    @Override
    protected void startGame() {
        table.setStatus(TableStatus.RUNNING);
        startNewRoundTimer();
    }

    @Override
    protected boolean canStartGame() {
        return table.getStatus() == TableStatus.WAITING_PLAYERS && table.getPlayers().size() >= 2;
    }

    private void sendHoleCardsToPlayers() {
        table.getRound().getPlayers().parallelStream().toList().forEach(this::notifyIndividualHoleCards);
    }

    private void notifyIndividualHoleCards(PokerPlayer toPlayer) {
        String message = PokerMapper.createHoleCardsMessage(Event.INITIAL_DEAL_DONE, table, toPlayer);
        voice.notifyMessage(toPlayer, message);
    }

    private void notifyPlayedAction(PokerActionType type, PokerPlayer player) {
        voice.notifyEverybody(type, player);
    }

    private void tryStartNewPokerRound() {
        try {
            croupierLock.lock();
            handleReloads();
            potHandler.clearPots();
            organizeSeatsAndPlayers();
            List<Seat<PokerPlayer>> roundCandidates = calculateRoundCandidates(table);
            List<PokerPlayer> newEntrants = filterNewEntrants(roundCandidates);
            HoldemRoundPlayers positions = calculatePokerPositions(roundCandidates);
            verifyRoundParticipantCount(positions.players());
            createRound(positions,  newEntrants);
            updateSkipCounts();
            blindsHandler.handleBlindBets(table);
            shuffleDeckAndDealHoleCards();
            assignPreFlopStarter();
            table.updateGamePhase(HoldemPhase.PRE_FLOP);
        } finally {
            croupierLock.unlock();
        }
    }

    private List<PokerPlayer> filterNewEntrants(List<Seat<PokerPlayer>> roundCandidates) {
        return roundCandidates.stream().filter(Seat::hasNewPlayer).map(Seat::getPlayer).toList();
    }

    private void organizeSeatsAndPlayers() {
        table.sanitizeSeatsByPlayerStatus(List.of(PlayerStatus.LEFT));
        table.getPlayers().forEach(PokerPlayer::prepareForNextRound);
    }

    private void shuffleDeckAndDealHoleCards() {
        deck = Deck.createAndShuffle();
        dealHoleCards();
    }

    private void assignPreFlopStarter() {
        changeTurn(calculatePreFlopStarter());
    }

    private PokerPlayer calculatePreFlopStarter() {
        if (table.getRound().isHeadsUp())
            return table.getRound().getSmallBlindPlayer();
        return getNextPlayerWithStatus(table.getRound().getPositions().bb(), List.of(PlayerStatus.ACTIVE)).orElseThrow();
    }

    public Optional<PokerPlayer> getNextPlayerWithStatus(PokerPlayer startingPointPlayer, List<PlayerStatus> allowedStatuses) {
        List<PokerPlayer> orderedRoundPlayers = HoldemFunctions.getPlayersInSeatOrderStartingFromYYYY(startingPointPlayer, table.getRound().getPlayers(), allowedStatuses);
        return orderedRoundPlayers.stream().filter(roundPlayer -> allowedStatuses.contains(roundPlayer.getStatus())).filter(roundPlayer -> !startingPointPlayer.equals(roundPlayer)).findFirst();
    }

    public Optional<PokerPlayer> getNextPlayerWhoCanAct(int fromSeat) {
        List<PokerPlayer> orderedRoundPlayers = HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(fromSeat, table.getRound().getPlayers(), List.of(PlayerStatus.ACTIVE, PlayerStatus.SIT_OUT, PlayerStatus.LEFT));
        return orderedRoundPlayers.stream().filter(canActAndIsNotSittingInSeat(fromSeat)).findFirst();
    }

    public Optional<PokerPlayer> getLastWhoCanActOnPhaseChange() {
        int button = table.getButton().getSeatNumber();
        List<PokerPlayer> orderedRoundPlayers = HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(button, table.getRound().getPlayers(), List.of(PlayerStatus.ACTIVE, PlayerStatus.SIT_OUT, PlayerStatus.LEFT));
        if (orderedRoundPlayers.get(0).getSeatNumber() == button)
            return Optional.of(orderedRoundPlayers.get(0));
        Collections.reverse(orderedRoundPlayers);
        return Optional.of(orderedRoundPlayers.get(0));
    }

    private static Predicate<PokerPlayer> canActAndIsNotSittingInSeat(int excludeSeatNumber) {
        return player -> player.canAct() && player.getSeatNumber() != excludeSeatNumber;
    }

    private HoldemRoundPlayers calculatePokerPositions(List<Seat<PokerPlayer>> roundAttendants) {
        if (roundAttendants.size() < 2)
            throw new IllegalPlayerCountException("not enough players to start calculations. was:" + roundAttendants.size()); // Basically not exceptional situation
        return isFirstRound() ? initFirstRound(roundAttendants) : initContinuationRound(roundAttendants);
    }

    private HoldemRoundPlayers initContinuationRound(List<Seat<PokerPlayer>> roundAttendants) {
        return PokerPositionsBuilder.of(table, roundAttendants.stream().map(Seat::getPlayer).collect(Collectors.toList())).build();
    }

    private boolean isFirstRound() {
        return table.getRounds().isEmpty();
    }

    private void createRound(HoldemRoundPlayers positions, List<PokerPlayer> newJoiners) {
        table.addRound(new PokerRound(positions,  newJoiners, table.getId()));
    }

    private void updateSkipCounts() {
        List<PokerPlayer> sitOuts = table.getPlayers().stream().filter(PokerPlayer::isSitOut).toList();
        sitOuts.forEach(PokerPlayer::increaseSkips);
    }

    private void handleTimedOutOrLeavingPlayer(PokerPlayer timedOutOrLeavingPlayer) {
        if (table.isActivePlayer(timedOutOrLeavingPlayer))
            autoPlay(timedOutOrLeavingPlayer);
    }

    @Override
    public Reloader getReloader() {
        return this.reloader;
    }

    @Override
    public void tearDownTable() {
        reloader.completePendingReloads();
    }

    @Override
    public BigDecimal countAllPlayersChipsOnTable() {
        PokerRound round = table.getRound();
        List<PokerPlayer> roundPlayers = round.getPlayers();
        return roundPlayers.stream().map(PokerPlayer::getTableChipCount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void handleReturningPlayer(PokerPlayer player) {
        try {
            table.getLock().lock();
            boolean immediate = table.getRound() != null && table.getRound().isPlayer(player);
            player.returnFromBreak(immediate);
            sendStatusUpdate(Event.STATUS_UPDATE);
            startTableIfPossible();
        } finally {
            table.getLock().unlock();
        }
    }

    @Override
    public void handleSitOut(PokerPlayer player, Boolean immediate) {
        player.sitOut(immediate);
        if (immediate)
            sendStatusUpdate(Event.STATUS_UPDATE);
    }

    @Override
    public PokerData getGameData() {
        return this.pokerInitData;
    }

    @Override
    public Integer getPlayerTurnTime() {
        return this.pokerInitData.playerTime();
    }

    private List<Seat<PokerPlayer>> calculateRoundCandidates(HoldemTable table) {
        if (pokerInitData.ante())
            return table.getNewAndActivePlayersSeatsCoveringAmount(pokerInitData.bigBlind().add(pokerInitData.anteAmount()));
        return table.getNewAndActivePlayersSeatsCoveringAmount(pokerInitData.bigBlind());
    }

    private void verifyRoundParticipantCount(List<PokerPlayer> roundPlayers) {
        if (roundPlayers.size() < 2)
            throw new IllegalStateException("Not enough valid players to start the round");
    }

    private HoldemRoundPlayers initFirstRound(List<Seat<PokerPlayer>> roundAttendants) {
        List<PokerPlayer> attendants = roundAttendants.stream().map(Seat::getPlayer).toList();
        return PokerPositionsBuilder.of(table, attendants).build();
    }

    public void sendStatusUpdate(Event event) {
        voice.notifyEverybody(event, null);
    }

    private void changeTurn(PokerPlayer player) {
        table.onActivePlayerChange(player);
    }

    private void dealHoleCards() {
        // Should not be run in parallel since the deal is sequential.
        PokerPlayer indexPlayer = table.getRound().getPositions().bb();
        for (int i = 0; i < 2 * table.getRound().getPlayers().size(); i++) {
            Optional<PokerPlayer> player = getNextPlayerWithStatus(indexPlayer, List.of(PlayerStatus.ACTIVE));
            if (player.isPresent()) {
                player.get().addHoleCard(deck.take());
                indexPlayer = player.get();
            }
        }
    }

    private void updateSitoutPlayers() {
        List<PokerPlayer> playersWithoutEnoughChips = table.getPlayers().stream().filter(player -> !player.coversAmount(pokerInitData.getMinBet())).toList();
        playersWithoutEnoughChips.forEach(player -> player.setStatus(PlayerStatus.SIT_OUT));
    }

    private void startNewRoundTimer() {
        getTable().startTiming(new NewRoundTask(getTable()), getGameData().getRoundDelay());
    }

    private void assignPokerHands() {
        List<PokerPlayer> playersWithHoleCards = table.getRound().getPlayers().stream().filter(PokerPlayer::hasHoleCards).toList();
        playersWithHoleCards.forEach(player -> player.createPokerHand(table.getRound().getTableCards()));
    }

    private void payForWinners(List<Pot> pots) {
        for (var pot : pots) {
            // TODO does it lose cents if division is not equal..
            BigDecimal winAmount = pot.getAmount().divide(BigDecimal.valueOf(pot.getWinners().size()), RoundingMode.DOWN).setScale(3, RoundingMode.DOWN);
            pot.getWinners().forEach(player -> player.increaseBalance(winAmount));
        }
    }

    @Override
    public void handleCall(PokerPlayer player) {
        table.stopTiming();
        BigDecimal callAmount = table.getRound().getMostChipsOnTable().subtract(player.getTableChipCount());
        player.call(callAmount);
        potHandler.addTableChipsCount(callAmount, player);
        notifyPlayedAction(PokerActionType.CALL, player);
        changePlayerOrPhase(player);
    }

    @Override
    public void handleCheck(PokerPlayer player) {
        table.stopTiming();
        player.check();
        notifyPlayedAction(PokerActionType.CHECK, player);
        changePlayerOrPhase(player);
    }

    @Override
    public void handleFold(PokerPlayer player) {
        table.stopTiming();
        potHandler.removePlayer(player);
        player.fold();
        notifyPlayedAction(PokerActionType.FOLD, player);
        changePlayerOrPhase(player);
    }

    @Override
    public void handleBetOrRaise(PokerPlayer player, BigDecimal raiseToAmount) {
        HoldemFunctions.verifyRaiseIsTechnicallyCorrect(player, raiseToAmount, table);
        table.stopTiming();
        clearActedFlagForPlayersWhoCanAct();
        if (shouldSetInitialRaiseAmount())
            setInitialRaiseAmount(raiseToAmount);
        BigDecimal missingFromPot = raiseToAmount.subtract(player.getTableChipCount());
        potHandler.addTableChipsCount(missingFromPot, player);
        player.betOrRaise(raiseToAmount);
        updateLastSpeakingPlayerFromAction(player);
        notifyPlayedAction(PokerActionType.BET_RAISE, player);
        table.getRound().setLastRaiserQQQ(player);
        Optional<PokerPlayer> nextPlayerOptional = getNextPlayerWhoCanAct(player.getSeatNumber());
        nextPlayerOptional.ifPresentOrElse(this::activateOrAutoplayPlayer, this::completeCurrentGamePhase);
    }

    private boolean shouldCompleteRound() {
        return table.getGamePhase() == HoldemPhase.RIVER || table.getRound().isWinnerKnown();
    }

    private void changePlayerOrPhase(PokerPlayer lastActor) {
        if (table.getRound().isWinnerKnown()) {
            completeCurrentGamePhase();
            return;
        }
        getNextPlayerWhoCanAct(lastActor.getSeatNumber()).ifPresentOrElse(this::activateOrAutoplayPlayer, this::completeCurrentGamePhase);
    }

    private void activateOrAutoplayPlayer(PokerPlayer actor) {
        changeTurn(actor);
        sendStatusUpdate(Event.STATUS_UPDATE);
        if (actor.shouldAutoPlay())
            autoPlay(actor);
    }

    private void autoPlay(PokerPlayer player) {
        if (HoldemFunctions.hasAction.apply(player, PokerActionType.CHECK))
            handleCheck(player);
        else
            handleFold(player);
    }

    private void setInitialRaiseAmount(BigDecimal raiseToAmount) {
        table.getRound().setInitialRaiseAmount(calculateInitialRaiseAmount(raiseToAmount));
    }

    private BigDecimal calculateInitialRaiseAmount(BigDecimal raiseToAmount) {
        if (isOpenTable())
            return raiseToAmount;
        return raiseToAmount.subtract(table.getDealer().getGameData().bigBlind());
    }

    private boolean shouldSetInitialRaiseAmount() {
        BigDecimal initialRaiseAmount = table.getRound().getInitialRaiseAmount();
        BigDecimal smallBlind = pokerInitData.smallBlind().setScale(2, RoundingMode.HALF_DOWN);
        return initialRaiseAmount.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN)) || initialRaiseAmount.setScale(2, RoundingMode.HALF_DOWN).equals(smallBlind);
    }

    private boolean isOpenTable() {
        return table.getRound().getPlayers().stream().filter(PokerPlayer::hasChipsOnTable).toList().isEmpty();
    }

    public PotHandler getPotHandler() {
        return potHandler;
    }

    public void handleAllIn(PokerPlayer player) {
        table.stopTiming();
        BigDecimal allInTotal = player.getCurrentBalance().add(player.getTableChipCount());
        if (Functions.isFirstMoreThanSecond.apply(allInTotal, table.getRound().getMostChipsOnTable())) {
            clearActedFlagForPlayersWhoCanAct();
            table.getRound().setLastRaiserQQQ(player);
        }
        if (isOpenTable())
            table.getRound().setInitialRaiseAmount(player.getTableChipCount());
        BigDecimal missingFromPot = player.getCurrentBalance().subtract(player.getTableChipCount());
        potHandler.addTableChipsCount(missingFromPot, player);
        player.allIn();
        notifyPlayedAction(PokerActionType.ALL_IN, player);
        updateLastSpeakingPlayerFromAction(player);
        Optional<PokerPlayer> nextPlayerOptional = getNextPlayerWhoCanAct(player.getSeatNumber());
        nextPlayerOptional.ifPresentOrElse(this::activateOrAutoplayPlayer, this::completeCurrentGamePhase);
    }

    private void updateLastSpeakingPlayerFromAction(PokerPlayer lastActor) {
        int index = table.getRound().getPlayers().indexOf(lastActor);
        if (index == 0)
            index = table.getRound().getPlayers().size() - 1;
        for (var notused : table.getRound().getPlayers()) {
            PokerPlayer indexPlayer = table.getRound().getPlayers().get(index);
            if (indexPlayer.canAct()) {
                table.getRound().setLastSpeakingPlayer(lastActor);
                return;
            }
            index--;
            if (index == -1)
                index = table.getRound().getPlayers().size() - 1;
        }
    }

    public void clearActedFlagForPlayersWhoCanAct() {
        List<PokerPlayer> playersWhoCanAct = table.getRound().getPlayers().stream().toList();
        playersWhoCanAct.forEach(PokerPlayer::clearActed);
    }

    private void completeCurrentGamePhase() {
        potHandler.onPhaseCompletion();
        if (shouldCompleteRound()) {
            completeRound();
            return;
        }
        if (shouldCompleteRoundWithShowdown()) {
        	showdown();
            completeRound();
            return;
        }
        preparePlayersForNextPhase();
        prepareNextGamePhase();
    }

    private void showdown() {
        showdown = true;
        table.clearActivePlayer();
        showdownHandler.executeWithDelay(this, 4000L, TimeUnit.MILLISECONDS);
        showdown = false;
    }

    public boolean isShowdown() {
        return showdown;
    }
     @Override
	  public void notifyPhaseCompleted() {
		   prepareNextGamePhase();
	  }
    
    private void prepareNextGamePhase() {
        table.getPhasePath().updateNext();
        table.getRound().setInitialRaiseAmount(BigDecimal.ZERO);
        addTableCards();
        sendStatusUpdate(Event.STATUS_UPDATE);
        if (shouldAutoPlayActivePlayer())
            autoPlay((PokerPlayer) table.getActivePlayer());
    }

    private boolean shouldAutoPlayActivePlayer() {
        return table.getActivePlayer() != null && (table.getActivePlayer().getStatus() == PlayerStatus.SIT_OUT || table.getActivePlayer().getStatus() == PlayerStatus.LEFT);
    }

    private boolean shouldCompleteRoundWithShowdown() {
        int allInPlayerCount = getAllInPlayerCount();
        return table.getRound().getPlayers().stream().filter(player -> player.hasBalance() && player.hasHoleCards()).count() <= 1 && allInPlayerCount >= 1;
    }

    private int getAllInPlayerCount() {
        return (int) table.getRound().getPlayers().stream().filter(PokerPlayer::isAllIn).count();
    }

    @Override
    public BigDecimal calculateMinRaise() {
        if (isOpenTable())
            return getGameData().bigBlind();
        BigDecimal bigBlind = getGameData().bigBlind();
        BigDecimal blindsAmount = bigBlind.add(getGameData().smallBlind()).setScale(2, RoundingMode.DOWN);
        if (table.getDealer().countAllPlayersChipsOnTable().equals(blindsAmount))// Can happen only on pre-flop phase
            return getGameData().bigBlind().multiply(BigDecimal.TWO);
        return table.getRound().getMostChipsOnTable().add(table.getRound().getInitialRaiseAmount());
    }

    @Override
    public boolean isAnyChipsOnTable() {
        return Functions.isFirstMoreThanSecond.apply(table.getRound().getMostChipsOnTable(), BigDecimal.ZERO);
    }

    private void addTableCards() {
        if (table.getGamePhase() == HoldemPhase.FLOP)
            table.getRound().setCards(deck.takeMany(3));
        else
            table.getRound().addCard(deck.take());
    }

    private void giveBackExtraChipsFromTable() {
        table.getRound().getPlayers().stream().filter(PokerPlayer::hasChipsOnTable).findFirst().ifPresent(PokerPlayer::takeChipsBackFromTable);
    }

    private void preparePlayersForNextPhase() {
        giveBackExtraChipsFromTable();
        HoldemRoundPlayers positions = table.getRound().getPositions();
        table.getRound().setLastSpeakingPlayer(getLastWhoCanActOnPhaseChange().orElseThrow());
        table.getRound().getPlayers().forEach(PokerPlayer::clearActed);
        if (table.getRound().isHeadsUp())
            changeTurn(table.getRound().getBigBlindPlayer());
        else
            changeTurn(getNextPlayerWhoCanAct(positions.buttonSeatNumber()).orElseThrow());
    }

    @Override
    public HoldemTable getTable() {
        return this.table;
    }

    public List<Pot> getPots() {
        return potHandler.getPots();
    }

    public CompletableFuture<Reload> handleReload(Reload reload) {
        try {
            CompletableFuture<Reload> reloadFuture = getReloader().addPendingReload(reload);
            croupierLock.lock();
            if (table.getStatus() == TableStatus.WAITING_PLAYERS) {
                getReloader().completePendingReloads();
                sendStatusUpdate(Event.STATUS_UPDATE);
            }
            return reloadFuture;
        } finally {
            croupierLock.unlock();
        }
    }
}
