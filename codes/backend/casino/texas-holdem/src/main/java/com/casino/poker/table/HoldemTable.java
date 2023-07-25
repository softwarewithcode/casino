package com.casino.poker.table;

import com.casino.common.cards.Card;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.message.Event;
import com.casino.common.player.PlayerStatus;
import com.casino.common.reload.Reload;
import com.casino.common.reload.ReloadData;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.Seat;
import com.casino.common.table.structure.SeatedTable;
import com.casino.common.user.User;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.dealer.HoldemDealer;
import com.casino.poker.export.NoLimitTexasHoldemAPI;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.game.PokerInitData;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.pot.Pot;
import com.casino.poker.round.PokerRound;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIgnoreProperties(value = {"dealer" /* explicitly ignoring dealer for not exposing deck to UI */})
@JsonIncludeProperties(value = {"type", "id", "activePlayer", "gamePhase", "watcherCount", "seats", "players", "counterTime", "tableCard", "status", "button", "pots", "tableCards"})
public final class HoldemTable extends SeatedTable<PokerPlayer> implements NoLimitTexasHoldemAPI, PokerTable<PokerPlayer> {
    private static final Logger LOGGER = Logger.getLogger(HoldemTable.class.getName());
    private final HoldemDealer dealer;
    private final DealerButton button;
    private final List<PokerRound> rounds;
    private final TableCard tableCard;

    public HoldemTable(TableData initData, PokerInitData pokerInitData) {
        super(initData);
        this.dealer = new HoldemDealer(this, pokerInitData);
        this.button = new DealerButton(null);
        this.rounds = new ArrayList<>(3);
        this.tableCard = new TableCard(initData, pokerInitData);
    }

    @Override
    public boolean join(User user, String seatNumber, Boolean waitBigBlind) {
        LOGGER.entering(getClass().getName(), "join", getId());
        try {
            PokerPlayer player = new HoldemPlayer(user, this); // rejoin case -> find existing
            player.setWaitBigBlind(waitBigBlind);
            Optional<Seat<PokerPlayer>> seatOptional = super.join(seatNumber, player);
            if (seatOptional.isPresent()) {
                player.setSeatNumber(seatOptional.get().getNumber());
                player.setStatus(PlayerStatus.NEW);
                dealer.onPlayerArrival(player);
            }
            return seatOptional.isPresent();
        } finally {
            LOGGER.exiting(getClass().getName(), "join" + " number:" + seatNumber + " bridge:" + user + " tableId:" + getId());
        }
    }

    @Override
    public void call(UUID playerId) {
        LOGGER.entering(getClass().getName(), "call", getId());
        try {
            getLock().lock();
            PokerPlayer player = getPlayer(playerId);
            verifyActionClearance(player, PokerActionType.CALL);
            dealer.call(player);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "call" + " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void check(UUID playerId) {
        LOGGER.entering(getClass().getName(), "check", getId());
        try {
            getLock().lock();
            PokerPlayer player = getPlayer(playerId);
            verifyActionClearance(player, PokerActionType.CHECK);
            dealer.check(player);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "check" + " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void leave(UUID leavingPlayerOrWatcher) {
        LOGGER.entering(getClass().getName(), "leave", getId());
        try {
            PokerPlayer leavingPlayer = getPlayer(leavingPlayerOrWatcher);
            if (leavingPlayer == null) {
                removeWatcher(leavingPlayerOrWatcher);
                return;
            }
            getLock().lock();// Only leaving players should lock the table. Leaving players affects gameplay.
            dealer.onPlayerLeave(leavingPlayer);
        } finally {
            if (getLock().isHeldByCurrentThread())
                getLock().unlock();
            LOGGER.exiting(getClass().getName(), "leave" + " player:" + leavingPlayerOrWatcher + " table:" + getId());
        }
    }

    @Override
    public void watch(User user) {
        LOGGER.entering(getClass().getName(), "watch", getId());
        PokerPlayer player = new HoldemPlayer(user, this);
        if (getPlayer(user.userId()) != null) {
            LOGGER.fine("User " + user.userName() + " is already playing in table:" + this);
            return;
        }
        if (super.joinAsWatcher(player))
            dealer.onWatcherArrival(player);
        LOGGER.exiting(getClass().getName(), "watch" + " player:" + getId() + " table:" + getId());
    }

    @Override
    public void raiseTo(UUID playerId, BigDecimal amount) {
        LOGGER.entering(getClass().getName(), "raiseTo", getId());
        try {
            getLock().lock();
            PokerPlayer player = getPlayer(playerId);
            verifyActionClearance(player, PokerActionType.BET_RAISE);
            dealer.betOrRaise(player, amount);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "raise" + " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void allIn(UUID playerId) {
        LOGGER.entering(getClass().getName(), "allIn", getId());
        try {
            getLock().lock();
            PokerPlayer player = getPlayer(playerId);
            verifyActionClearance(player, PokerActionType.ALL_IN);
            dealer.allIn(player);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "raise" + " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void fold(UUID playerId) {
        LOGGER.entering(getClass().getName(), "fold", getId());
        try {
            getLock().lock();
            PokerPlayer player = getPlayer(playerId);
            verifyActionClearance(player, PokerActionType.FOLD);
            dealer.fold(player);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "fold" + " player:" + playerId + " table:" + getId());
        }
    }

    private void verifyActionClearance(PokerPlayer player, PokerActionType action) {
        if (!isPlayerAllowedToAct(player, action)) {
            LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: '" + action + "' playerInTurn:" + getActivePlayer() + " phase: " + getGamePhase() + " in table:" + this);
            throw new IllegalPlayerActionException(action + " not allowed for player:" + player + " activePlayer:" + getActivePlayer());
        }
    }

    private boolean isPlayerAllowedToAct(PokerPlayer player, PokerActionType pokerAction) {
        verifyPlayerHasSeat(player);
        return isActivePlayer(player) && HoldemFunctions.hasAction.apply(player, pokerAction) && getGamePhase().isRunning();
    }

    @Override
    public void refresh(UUID id) {
        dealer.notifyTableStatus(Event.STATUS_UPDATE);
    }

    @Override
    public HoldemDealer getDealer() {
        return dealer;
    }

    @Override
    public TableCard getTableCard() {
        List<Integer> seats = getSeats().stream().filter(seat -> !seat.hasPlayer()).map(Seat::getNumber).toList();
        tableCard.setAvailablePositions(seats);
        return tableCard;
    }

    public List<PokerRound> getRounds() {
        return rounds;
    }

    @Override
    public void sitOutNextHand(UUID playerId) {
        PokerPlayer player = getPlayer(playerId);
        Objects.requireNonNull(player);
        dealer.sitOut(player, false); // does not affect game immediately, no lock
    }

    @Override
    public void continueGame(UUID playerId) {
        try {
            getLock().lock(); // continuing can affect game immediately
            PokerPlayer player = getPlayer(playerId);
            Objects.requireNonNull(player);
            dealer.continueGame(player);
        } finally {
            getLock().unlock();
            LOGGER.exiting(getClass().getName(), "continueGame" + " player:" + playerId + " table:" + getId());
        }
    }

    public PokerRound getRound() {
        if (rounds.size() == 0)
            return null;
        return rounds.get(rounds.size() - 1);
    }

    public PokerRound getPreviousRound() {
        if (rounds.size() < 2)
            return null;
        return rounds.get(rounds.size() - 2);
    }

    public void addRound(PokerRound round) {
        rounds.add(round);
    }

    @Override
    public boolean toggleWaitBigBlind(UUID id) {
        PokerPlayer player = getPlayer(id);
        if (player.getStatus() != PlayerStatus.NEW)
            throw new IllegalArgumentException("Wrong status, should be sitOut?? or new. TODO Was:" + player.getStatus());
        boolean opposite = !player.isWaitingBigBlind();
        player.setWaitBigBlind(opposite);
        return opposite;
    }

    @Override
    public CompletableFuture<Reload> reload(UUID playerId, UUID reloadId, BigDecimal amount) {
        LOGGER.entering(getClass().getName(), "reload", getId());
        try {
            HoldemPlayer player = (HoldemPlayer) getPlayer(playerId);
            Objects.requireNonNull(player);
            ReloadData data = new ReloadData(reloadId, player, amount, dealer.getGameData().maxBuyIn());
            Reload reload = new Reload(data);
            return dealer.handleReload(reload);
        } finally {
            LOGGER.exiting(getClass().getName(), "reload" + " table:" + getId());
        }
    }

    public DealerButton getButton() {
        return button;
    }

    @Override
    public HoldemPhase getGamePhase() {
        return (HoldemPhase) phasePath.getPhase(); // move to round
    }

    @Override
    public synchronized void onClose() {
        setStatus(TableStatus.CLOSING);
        dealer.tearDown();
        super.onClose();
    }

    public List<Pot> getPots() {
        return dealer.getPots();
    }

    public List<Card> getTableCards() { // For serialization (UI)
        return getRound() == null ? Collections.emptyList() : getRound().getTableCards();
    }
}
