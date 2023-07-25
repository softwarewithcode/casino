package com.casino.blackjack.dealer;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.casino.blackjack.bank.BlackjackBank;
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.game.BlackjackInitData;
import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.dealer.CommunicationChannel;
import com.casino.common.game.phase.bet.ParallelBetPhase;
import com.casino.common.game.phase.bet.ParallelBetPhaser;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.dealer.CardDealer;
import com.casino.common.dealer.Notifier;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.game.phase.insurance.InsurancePhase;
import com.casino.common.game.phase.insurance.InsurancePhaser;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.structure.Seat;
import com.casino.blackjack.util.BlackjackUtil;
import com.casino.common.message.Event;
import com.casino.common.table.TableStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIgnoreType
@JsonIncludeProperties(value = { /* explicitly nothing from here */})
public class BlackjackDealer implements CardDealer, ParallelBetPhaser, InsurancePhaser {
    private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());

    private final BlackjackTable table;
    private final ReentrantLock betPhaseLock;
    private final CommunicationChannel voice;
    private List<Card> deck;
    private BlackjackDealerHand dealerHand;
    private final BlackjackInitData blackjackInitData;

    public BlackjackDealer(BlackjackTable blackjackTable, BlackjackInitData gameData) {
        this.table = blackjackTable;
        this.deck = Deck.pileUpAndShuffle(8);
        this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
        this.betPhaseLock = new ReentrantLock();
        this.voice = new Notifier(table);
        this.blackjackInitData = gameData;
    }

    private void startBetPhaseClock(long initialDelay) {
        ParallelBetPhase<BlackjackDealer> task = new ParallelBetPhase<>(this);
        getTable().startTiming(task, initialDelay);
    }

    public void startInsurancePhase() {
        table.updateGamePhase(BlackjackGamePhase.INSURE);
        InsurancePhase<BlackjackDealer> task = new InsurancePhase<>(this);
        getTable().startTiming(task, 0);
        voice.notifyAll(Event.INSURANCE_TIME_START, null);
    }

    private boolean hasStartingAce() {
        Card card = dealerHand.getCards().get(0);
        return card != null && card.isAce();
    }

    public void updatePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
        Stream<Seat<BlackjackPlayer>> seatStream = table.getSeats().stream();
        Optional<Seat<BlackjackPlayer>> seatOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
        Seat<BlackjackPlayer> seat = seatOptional.orElseThrow(PlayerNotFoundException::new);
        seat.getPlayer().updateStartingBet(bet);
    }

    public BlackjackDealerHand getHand() {
        return dealerHand;
    }

    public boolean isRoundCompleted() {
        return table.getGamePhase() == BlackjackGamePhase.ROUND_COMPLETED;
    }

    public List<Card> getDecks() {
        return deck;
    }

    @Override
    public BlackjackTable getTable() {
        return table;
    }

    public void dealStartingHands() {
        LOGGER.fine("BlackjackDealer deals starting hands in table:" + getTable());
        List<BlackjackPlayer> orderedPlayers = table.getOrderedPlayersWithBet();
        orderedPlayers.forEach(player -> dealCard(deck, player.getActiveHand())); // first the players
        dealCard(deck, dealerHand); // then the dealer
        orderedPlayers.forEach(player -> dealCard(deck, player.getActiveHand())); // then the players again
    }

    public void hit(BlackjackPlayer player) {
        table.verifyPlayerHasSeat(player);
        dealCard(deck, player.getActiveHand());
        if (shouldActivateSecondHand(player)) {
            player.activateSecondHand();
            dealCard(deck, player.getActiveHand());
        }
    }

    private boolean shouldActivateSecondHand(BlackjackPlayer player) {
        return player.getHands().get(0).isCompleted() && player.getHands().size() == 2 && !player.getHands().get(1).isActive() && !player.getHands().get(1).isCompleted();
    }

    @Override
    public <T extends ICasinoPlayer> void onPlayerArrival(T player) {
        try {
            voice.notifyPlayerArrival(player);
            if (!betPhaseLock.tryLock()) {
                voice.notifyPlayer(Event.STATUS_UPDATE, player);
                return;
            }
            if (shouldStartNewGame()) {
                startNewGame();
                voice.notifyAll(Event.BET_TIME_START, player);
            }
        } finally {
            if (betPhaseLock.isHeldByCurrentThread())
                betPhaseLock.unlock();
        }
    }

    private void startNewGame() {
        table.setStatus(TableStatus.RUNNING);
        table.updateGamePhase(BlackjackGamePhase.BET);
        dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
        startBetPhaseClock(0L);
    }

    private boolean shouldStartNewGame() {
        return table.getStatus() == TableStatus.WAITING_PLAYERS || table.getActivePlayerCount() == 1;
    }

    private boolean shouldDealStartingHands() {
        return table.isGamePhase(BlackjackGamePhase.BETS_COMPLETED) && hasSomebodyBet() && isEnoughCardsForPlayersAndDealer();
    }

    private boolean isEnoughCardsForPlayersAndDealer() {
        // Amount of decks used can vary. For example using 100 decks combined.
        int dealer = 1;
        return (table.getPlayers().size() + dealer) * getMaximumNumberOfCardsPlayerCanHold() < deck.size();
    }

    private static int getMaximumNumberOfCardsPlayerCanHold() {
        return 11; // 11 aces=21 (6 decks contains 24 aces)
    }

    private boolean hasSomebodyBet() {
        List<BlackjackPlayer> c = table.getOrderedPlayersWithBet();
        return c != null && c.size() > 0;
    }

    public synchronized void finalizeBetPhase() {
        table.updateGamePhase(BlackjackGamePhase.BETS_COMPLETED);
        updatePlayerStatuses();
        if (!shouldDealStartingHands()) {
            LOGGER.fine("dealer does not deal cards now");
            table.updatePlayersToWatchers(true);
            voice.notifyAll(Event.NO_BETS_NO_DEAL, null);
            return;
        }
        table.updatePlayersToWatchers(false);
        matchPlayersBalancesWithBets();
        dealStartingHands();
        if (hasStartingAce()) {
            startInsurancePhase();
            return;
        }
        table.updateGamePhase(BlackjackGamePhase.PLAY);
        updateActorAndClock();
        voice.notifyAll(Event.INITIAL_DEAL_DONE, table.getActivePlayer());
    }

    private void matchPlayersBalancesWithBets() {
        table.getPlayersWithBet().forEach(this::matchPlayerBalanceAccordingToBet);
    }

    private void matchPlayerBalanceAccordingToBet(BlackjackPlayer player) {
        BlackjackHand hand = player.getActiveHand();
        hand.updateBet(player.getTotalBet());
        player.subtractTotalBetFromBalance();
    }

    private void updatePlayerStatuses() {
        List<BlackjackPlayer> players = table.getSeats().stream().filter(Seat::hasPlayer).map(Seat::getPlayer).toList();
        players.forEach(this::updatePlayerStatus);
    }

    private void updatePlayerStatus(ICasinoPlayer player) {
        if (player.getStatus() == PlayerStatus.LEFT)
            return;
        if (player.hasBet()) {
            player.setStatus(PlayerStatus.ACTIVE);
            player.clearSkips();
        } else {
            player.setStatus(PlayerStatus.SIT_OUT_NEXT_HAND);
            player.increaseSkips();
        }
    }

    private void updateActorAndClock() {
        table.stopClock();
        Optional<Seat<BlackjackPlayer>> optionalPlayerSeat = table.getSeats().stream().filter(Seat::hasActivePlayerWithBet).min(Comparator.comparing(Seat::getNumber));
        optionalPlayerSeat.ifPresentOrElse(seat -> table.onActivePlayerChange(seat.getPlayer()), this::onDealerTurnChange);
    }

    private void completeRound() {
        try {
            if (table.getGamePhase() != BlackjackGamePhase.PLAY) {
                LOGGER.severe("complete round called in a wrong phase was: " + table.getGamePhase());
                throw new IllegalStateException("cannot complete round");
            }
            completeActiveHands();
            addDealerCards();
            BlackjackBank.matchBalances(table.getPlayersWithBet(), dealerHand);
            table.updateGamePhase(BlackjackGamePhase.ROUND_COMPLETED);
            if (table.getActivePlayerCount() == 0)
                table.setStatus(TableStatus.WAITING_PLAYERS);
            if (shouldRestartBetPhase())
                restartBetPhase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Something unexpected happened. Waiting for brush to arrive.", e);
            BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
            table.onClose();
            throw new IllegalStateException("what to do");
        }
    }

    private void restartBetPhase() {
        table.updateCounterTime(this.blackjackInitData.getRoundDelay().intValue());
        startBetPhaseClock(this.blackjackInitData.getRoundDelay());
    }

    private void sanitizeInactiveSeats() {
        table.findInactivePlayerSeats().forEach(Seat::sanitize);
    }

    public void handlePlayerComeBack(CasinoPlayer player) {
        voice.notifyPlayer(Event.STATUS_UPDATE, player);
    }

    public synchronized void prepareBetPhase() {
        if (table.getGamePhase() != BlackjackGamePhase.ROUND_COMPLETED)
            throw new IllegalArgumentException("not allowed");
        table.getPlayers().forEach(ICasinoPlayer::prepareForNextRound);
        this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
        table.updateGamePhase(BlackjackGamePhase.BET);
        table.updateCounterTime(getBetPhaseTime());
        deck = Deck.pileUpAndShuffle(8);
        voice.notifyAll(Event.BET_TIME_START, null);
    }

    private boolean shouldRestartBetPhase() {
        return table.getStatus() == TableStatus.RUNNING && table.getActivePlayerCount() > 0 && table.getGamePhase() == BlackjackGamePhase.ROUND_COMPLETED;
    }

    private void onDealerTurnChange() {
        table.updateDealerTurn(true);
        table.clearActivePlayer();
        completeRound();
        voice.notifyAll(Event.ROUND_COMPLETED, null);
    }

    public void doubleDown(BlackjackPlayer player) {
        table.verifyPlayerHasSeat(player);
        Card cardReference = deck.get(deck.size() - 1); // TODO could deal card here without lock? Balance check is done in doubleDown method
        player.doubleDown(cardReference);
        removeLastCardFromDeck(deck);
    }

    public Card getNextCard() {
        return deck.get(deck.size() - 1);
    }

    public void handleSplit(BlackjackPlayer player) {
        table.verifyPlayerHasSeat(player);
        player.splitStartingHand();
        BlackjackHand firstHand = player.getActiveHand();
        dealCard(deck, player.getFirstHand());
        if (firstHand.isCompleted()) {
            player.getHands().get(1).activate();
            dealCard(deck, player.getHands().get(1));
        }
    }

    private void autoplayForPlayer(BlackjackPlayer player) {
        Card nextCard = getNextCard();
        if (player.autoplay(nextCard).isEmpty())
            removeLastCardFromDeck(deck);
    }

    public void stand(BlackjackPlayer player) {
        table.verifyPlayerHasSeat(player);
        player.stand();
        if (shouldActivateSecondHand(player)) {
            player.activateSecondHand();
            dealCard(deck, player.getActiveHand());
        }
    }

    public void insure(BlackjackPlayer player) {
        table.verifyPlayerHasSeat(player);
        player.insure();
    }

    private void completeActiveHands() {
        List<BlackjackPlayer> players = table.getPlayersWithBet().stream().filter(ICasinoPlayer::hasActiveHand).toList();
        players.forEach(player -> player.getActiveHand().complete());
    }

    private void addDealerCards() {
        while (!dealerHand.isCompleted()) {
            LOGGER.fine("Dealer takes cards:");
            dealCard(deck, dealerHand);
        }
    }

    private void finalizeInsurancePhase() {
        table.updateGamePhase(BlackjackGamePhase.PLAY);
        updateActorAndClock();
        Event title = table.getActivePlayer() != null ? Event.PLAYER_TIME_START : Event.ROUND_COMPLETED;
        voice.notifyAll(title, table.getActivePlayer());
    }

    public void updateActorAndNotify() {
        updateActorAndClock();
        if (table.getActivePlayer() != null)
            voice.notifyAll(Event.PLAYER_TIME_START, table.getActivePlayer());
    }

    public void onPlayerLeave(BlackjackPlayer leavingPlayer) {
        leavingPlayer.setStatus(PlayerStatus.LEFT);
        if (table.isActivePlayer(leavingPlayer)) {
            finalizeInactivePlayerTurn(leavingPlayer);
            updateActorAndNotify();
        }
        if (!table.isRoundRunning()) {
            sanitizeInactiveSeats();
            voice.notifyAll(Event.PLAYER_LEFT, leavingPlayer);
        } else
            voice.notifyAll(Event.SIT_OUT, leavingPlayer);
        if (!table.hasPlayers()) {
            table.stopClock();
            table.setStatus(TableStatus.WAITING_PLAYERS);
        }
    }

    private void finalizeInactivePlayerTurn(BlackjackPlayer player) {
        if (player.hasBet() && table.isActivePlayer(player))
            autoplayForPlayer(player);
    }

    private void handleTimedOutPlayer(BlackjackPlayer timedOutPlayer) {
        finalizeInactivePlayerTurn(timedOutPlayer);
        voice.notifyAll(Event.TIMED_OUT, timedOutPlayer);
        updateActorAndNotify();
    }

    @Override
    public <T extends ICasinoPlayer> void onWatcherArrival(T watcher) {
        voice.notifyTableOpening(watcher);
    }

    @Override
    public boolean shouldPrepareBetPhase() {
        return table.getCounterTime() == getBetPhaseTime() && table.getGamePhase() == BlackjackGamePhase.ROUND_COMPLETED;
    }

    @Override
    public void onPlayerTimeout(ICasinoPlayer timedOutPlayer) {
        LOGGER.entering(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer);
        try {
            table.lock();
            if (!table.isActivePlayer(timedOutPlayer))
                return;
            handleTimedOutPlayer((BlackjackPlayer) timedOutPlayer);
        } finally {
            table.unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer);
        }
    }

    @Override
    public BlackjackInitData getGameData() {
        return this.blackjackInitData;
    }

    @Override
    public void onInsurancePhaseEnd() {
        LOGGER.entering(getClass().getName(), "onInsurancePhaseEnd" + getTable());
        try {
            table.lock();
            if (!table.isGamePhase(BlackjackGamePhase.INSURE))
                throw new IllegalPhaseException("GamePhase is not what is expected on insurancePhaseEnd", table.getGamePhase().toString(), BlackjackGamePhase.INSURE.toString());
            finalizeInsurancePhase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "onInsurancePhaseEnd() something went wrong. Waiting for manager's call.", e);
            BlackjackUtil.dumpTable(getTable(), "onBetPhaseEnd");
            table.updateGamePhase(BlackjackGamePhase.ERROR);
        } finally {
            table.getLock().unlock();
            LOGGER.exiting(getClass().getName(), "onInsurancePhaseEnd" + getTable());
        }
    }

    @Override
    public Integer getInsurancePhaseTime() {
        return this.blackjackInitData.insurancePhaseTime();
    }

    @Override
    public void onBetPhaseEnd() {
        try {
            table.lock();
            if (!table.isGamePhase(BlackjackGamePhase.BET))
                throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", table.getGamePhase().toString(), BlackjackGamePhase.BET.toString());
            finalizeBetPhase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
            BlackjackUtil.dumpTable(getTable(), "onBetPhaseEnd");
            table.updateGamePhase(BlackjackGamePhase.ERROR);
        } finally {
            table.unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "onBetPhaseEnd" + this.getTableId());
        }
    }

    @Override
    public Integer getBetPhaseTime() {
        return this.blackjackInitData.betPhaseTime();
    }

    @Override
    public void updateCounterTime(Integer time) {
        table.updateCounterTime(time);
    }

    @Override
    public Integer getCounterTime() {
        return table.getCounterTime();
    }

    @Override
    public boolean isClockTicking() {
        return table.isClockTicking();
    }

    @Override
    public void stopClock() {
        table.stopClock();
    }

    @Override
    public UUID getTableId() {
        return getTable().getId();
    }

    @Override
    public int getPlayerTurnTime() {
        return this.blackjackInitData.playerTime();
    }

    @JsonProperty//Getter for serialization
    public BlackjackDealerHand getDealerHand() {
        return dealerHand;
    }
}
