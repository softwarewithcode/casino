package com.casino.roulette.croupier;

import com.casino.common.dealer.TableGameCroupier;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.functions.Functions;
import com.casino.common.game.phase.bet.ParallelBetPhaser;
import com.casino.common.message.Event;
import com.casino.common.message.MessageTitle;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.table.structure.Seat;
import com.casino.common.user.Connectable;
import com.casino.common.validation.Verifier;
import com.casino.roulette.bet.Bet;
import com.casino.roulette.bet.BetData;
import com.casino.roulette.export.BetType;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.casino.roulette.export.RoulettePlayerAction;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.game.RouletteGamePhase;
import com.casino.roulette.persistence.SpinResult;
import com.casino.roulette.player.RoulettePlayer;
import com.casino.roulette.table.RouletteTable_;
import com.casino.roulette.table.RouletteWheel;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author softwarewithcode from GitHub <br>
 * <br>
 * <p>
 * Croupier handles single and multiplayer roulettetable game as the mainController. <br>
 * Spinning the wheel i.e. getting random number is handled in asynchronous manner.
 */
@JsonIgnoreType
@JsonIncludeProperties(value = { /* explicitly nothing from here */})
public final class Croupier extends TableGameCroupier implements RouletteCroupier, ParallelBetPhaser {
    private static final Logger LOGGER = Logger.getLogger(Croupier.class.getName());
    private final RouletteTable_ table;
    private final RouletteData initData;

    public Croupier(RouletteTable_ table, RouletteData initData) {
        super(table);
        this.table = table;
        this.initData = initData;
        table.updateGamePhase(null);
    }

    @Override
    public CasinoTable getTable() {
        return table;
    }

    @Override
    protected void startGame() {
        table.setStatus(TableStatus.RUNNING);
        startBetPhase();
    }

    @Override
    public <T extends CasinoPlayer> void onPlayerLeave(T leavingPlayer) {
        var player = table.getPlayer(leavingPlayer.getId());
        try {
            croupierLock.lock();
            leavingPlayer.setStatus(PlayerStatus.LEFT);
            if (shouldRemoveBets(player))
                player.removeBets(true);
            if (canSanitizePlayerSeat(player)) {
                sanitizeInactiveSeats();
                notifyEverybodyAndUpdatePlayerAvailableActions(Event.PLAYER_LEFT, leavingPlayer);
            }
            if (!table.hasPlayers()) {
                table.stopTiming();
                table.setStatus(TableStatus.WAITING_PLAYERS);
            }
        } finally {
            croupierLock.unlock();
        }
    }


    private boolean shouldRemoveBets(RoulettePlayer player) {
        return table.getGamePhase() == RouletteGamePhase.BET && player.hasBet() && !player.isActive();
    }

    private boolean canSanitizePlayerSeat(RoulettePlayer player) {
        if (player.hasBet() || player.isActive())
            return false;
        return table.getGamePhase() == RouletteGamePhase.BET;
    }

    private void sanitizeInactiveSeats() { // lock
        table.findInactivePlayerSeats().forEach(Seat::sanitize); // removes now all inactives instead of only leaving player
    }

    @Override
    public void onBetPhaseEnd() {
        LOGGER.fine("RouletteTable onBetPhaseEnd");
        try {
            croupierLock.lock();
            play(); // lock releases while spinning
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
            table.updateGamePhase(RouletteGamePhase.ERROR);
        } finally {
            croupierLock.unlock();
            LOGGER.exiting(getClass().getName(), "onBetPhaseEnd" + this.getTableId());
        }
    }

    @Override
    public void handleSinglePlayerSpinRequest(RoulettePlayer player, UUID spinId) {
        try {
            tryLockOrThrow();
            Verifier.verifyId(spinId, table.getWheel().getSpinId());
            player.updateAvailableActions();
            if (!Functions.containsAction(player.getActions(), RoulettePlayerAction.PLAY))
                throw new IllegalPlayerActionException("Cannot invoke play:" + player);
            play();
        } finally {
            if (croupierLock.isHeldByCurrentThread())
                croupierLock.unlock();
        }
    }

    private void play() {
        LOGGER.fine("Play in table:" + getTableId());
        finalizeBetPhase();
        if (isBetPhaseCompleted())
            playRound().thenRun(this::tryRestartRound);
        else {
            addSkippedRoundResult();
            tryRestartRound();
        }
    }

    private void addSkippedRoundResult() {
        SpinResult res = new SpinResult(table.getWheel().getSpinId(), null, null);
        table.getPlayers().forEach(player -> player.onRoundCompletion(res));
    }

    private boolean isBetPhaseCompleted() {
        return table.getGamePhase() == RouletteGamePhase.BETS_COMPLETED;
    }

    private void finalizeBetPhase() {
        try {
            croupierLock.lock();
            Verifier.verifyGamePhase(table, RouletteGamePhase.BET);
            table.updateGamePhase(RouletteGamePhase.BETS_COMPLETED);
            if (!hasAnyBetPlaced()) {
                table.updateGamePhase(RouletteGamePhase.ROUND_COMPLETED);
                LOGGER.fine("finalizeBetPhase playerCount: " + table.getPlayerCount());
                notifyEverybodyAndUpdatePlayerAvailableActions(Event.NO_BETS_NO_DEAL, null);
            }
        } finally {
            croupierLock.unlock();
        }
    }

    private CompletableFuture<Void> playRound() {
        try {
            LOGGER.fine(("PlayRound in table:" + getTableId()));
            croupierLock.lock();
            Verifier.verifyGamePhase(table, RouletteGamePhase.BETS_COMPLETED);
            CompletableFuture<Void> round = new CompletableFuture<>();
            proceedToSpinPhase();
            throwBallIntoBallTrack().thenRun(this::completeRound).thenRun(() -> round.complete(null));
            return round;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Something unexpected happened. Waiting for brush to arrive.", e);
            table.updateGamePhase(RouletteGamePhase.ERROR);
            table.onClose();
            throw new IllegalStateException("Table " + table.getId() + " cannot continue. Last known number:" + table.getWheel().getResult() + " and spinId:" + table.getWheel().getSpinId());
        } finally {
            croupierLock.unlock();
        }
    }

    private void notifyEverybodyAndUpdatePlayerAvailableActions(MessageTitle title, Connectable c) {
        table.getPlayers().forEach(CasinoPlayer::updateAvailableActions);
        voice.notifyEverybody(title, c);
    }

    private void notifyUpdateAndUpdateAvailableActions() {
        table.getPlayers().forEach(CasinoPlayer::updateAvailableActions);
        voice.notifyUpdate();
    }

    private void completeRound() {
        try {
            croupierLock.lock();
            table.updateGamePhase(RouletteGamePhase.ROUND_COMPLETED);
            payout();
            RouletteWheel wheel = table.getWheel();
            table.getPlayers().forEach(player -> player.onRoundCompletion(wheel.getResult()));
            notifyEverybodyAndUpdatePlayerAvailableActions(Event.ROUND_COMPLETED, null);
        } finally {
            croupierLock.unlock();
        }
    }

    private CompletableFuture<SpinResult> throwBallIntoBallTrack() {
        return table.getWheel().spinBall();
    }

    private void proceedToSpinPhase() {
        try {
            croupierLock.lock();
            table.updateGamePhase(RouletteGamePhase.SPINNING);
            notifyEverybodyAndUpdatePlayerAvailableActions(Event.SPINNING, null);
        } finally {
            croupierLock.unlock();
        }
    }

    private void startBetPhase() {
        try {
            croupierLock.lock();
            LOGGER.fine("RouletteTable starting betPhase");
            table.updateGamePhase(RouletteGamePhase.BET);
            if (table.isMultiplayer()) {
                table.updateCounterTime(this.initData.getRoundDelay().intValue());
                // 500l is delay before starting first round
                long roundDelay = table.getWheel().getResult() == null ? 500L : this.initData.getRoundDelay();
                startParallelBetPhase(roundDelay);
            }
            notifyEverybodyAndUpdatePlayerAvailableActions(Event.BET_TIME_START, null);
        } finally {
            croupierLock.unlock();
        }
    }

    private boolean hasAnyBetPlaced() {
        return !table.getPlayersWithBet().isEmpty();
    }

    private void payout() {
        List<RoulettePlayer> players = table.getOrderedPlayersWithBet();
        players.forEach(player -> player.updateBalanceAndWinnings(table.getWheel().getResult().winningNumber()));
    }

    @Override
    public Integer getBetPhaseTime() {
        return initData.betPhaseTime();
    }

    @Override
    public boolean shouldRestartBetPhase() {
        return table.getStatus() == TableStatus.RUNNING && table.getActivePlayerCount() > 0 && table.getGamePhase() == RouletteGamePhase.ROUND_COMPLETED;
    }

    @Override
    public void reInitializeBetPhase() {
        if (table.getGamePhase() != RouletteGamePhase.ROUND_COMPLETED)
            throw new IllegalArgumentException("not allowed");
        tryRestartRound();
    }

    @Override
    public UUID getTableId() {
        return getTable().getId();
    }

    @Override
    public RouletteData getGameData() {
        return initData;
    }

    @Override
    public void handleChipAddition(RoulettePlayer player, Integer positionNumber, BigDecimal amount) {
        Verifier.verifyGamePhase(table, RouletteGamePhase.BET);
        BetType betType = EuropeanRouletteTable.verifyPositionAndGetBetPositionType(positionNumber);
        BetData data = new BetData(UUID.randomUUID(), amount, betType, positionNumber);
        Bet bet = new Bet(data);
        player.bet(bet, getGameData().betRange());
        notifyUpdateAndUpdateAvailableActions();
    }

    @Override
    public void handleChipRemoval(RoulettePlayer player, Boolean removeAllBets) {
        Verifier.verifyGamePhase(table, RouletteGamePhase.BET);
        player.removeBets(removeAllBets);
        notifyUpdateAndUpdateAvailableActions();
    }

    @Override
    public void handleChipsRemovalFromPosition(RoulettePlayer player, Integer position) {
        Verifier.verifyGamePhase(table, RouletteGamePhase.BET);
        EuropeanRouletteTable.verifyPositionAndGetBetPositionType(position);
        player.removeBetsFromPosition(position);
        notifyUpdateAndUpdateAvailableActions();
    }

    @Override
    public void handlePreviousRoundChipsRepetition(RoulettePlayer player) {
        Verifier.verifyGamePhase(table, RouletteGamePhase.BET);
        player.repeatLastBets(getGameData().betRange());
        notifyUpdateAndUpdateAvailableActions();
    }

    private void tryRestartRound() {
        table.prepareForNextRound();
        if (shouldRestartBetPhase())
            startBetPhase();
    }
}
