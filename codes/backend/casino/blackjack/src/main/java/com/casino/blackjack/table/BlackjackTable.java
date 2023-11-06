package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.dealer.Dealer;
import com.casino.blackjack.export.BlackjackTableAPI;
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.game.BlackjackData;
import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer_;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.CardPlayer;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableData;
import com.casino.common.table.structure.Seat;
import com.casino.common.table.structure.SeatedTable;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIgnoreProperties(value = {"dealer" /* explicitly ignoring dealer for not exposing deck to UI */})
@JsonIncludeProperties(value = {"type", "id", "activePlayer", "gamePhase", "watcherCount", "seats", "players", "counterTime", "tableCard", "dealerHand"})
public final class BlackjackTable extends SeatedTable<BlackjackPlayer_> implements BlackjackTableAPI {
    private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
    private final Dealer dealer;

    public BlackjackTable(TableData initData, BlackjackData blackjackData) {
        super(initData);
        super.tableCard = new TableCard<>(initData, blackjackData);
        this.dealer = new Dealer(this, blackjackData);
    }

    @Override
    public boolean join(User user, String seatNumber) {
        LOGGER.entering(getClass().getName(), "join", getId());
        try {
            BlackjackPlayer_ player = new BlackjackPlayer_(user, this);
            Optional<Seat<BlackjackPlayer_>> seatOptional = super.join(seatNumber, player);
            if (seatOptional.isPresent()) {
                player.setSeatNumber(seatOptional.get().getNumber());
                dealer.onPlayerArrival(player);
            }
            return seatOptional.isPresent();
        } finally {
            LOGGER.exiting(getClass().getName(), "join" + " number:" + seatNumber + " bridge:" + user + " tableId:" + getId());
        }
    }

    @Override
    public void bet(UUID playerId, BigDecimal bet) {
        LOGGER.entering(getClass().getName(), "bet", bet + " player:" + playerId + " table:" + getId());
        try {
            BlackjackPlayer_ player = getPlayer(playerId);
            if (isGamePhase(BlackjackGamePhase.BET))
                dealer.handleBet(player, bet);
            else {
                LOGGER.severe("Starting bet is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
                throw new IllegalPlayerActionException("placeStartingBet is not allowed:" + player + " bet:" + bet.toString());
            }
        } finally {
            LOGGER.exiting(getClass().getName(), "bet", bet + " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void insure(UUID playerId) {
        LOGGER.entering(getClass().getName(), "insure", " player:" + playerId + " table:" + getId());
        try {
            BlackjackPlayer_ player = getPlayer(playerId);
            if (isGamePhase(BlackjackGamePhase.INSURE))
                dealer.handleInsure(player);
            else {
                LOGGER.severe("insuring is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
                throw new IllegalPlayerActionException("insuring is not allowed in phase: " + getGamePhase() + " table:" + this + " player:" + player);
            }
        } finally {
            LOGGER.exiting(getClass().getName(), "insure", " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void stand(UUID playerId) {
        LOGGER.entering(getClass().getName(), "stand", " player:" + playerId + " table:" + getId());
        try {
            lock();// Lock releases immediately if player is not in turn
            BlackjackPlayer_ player = getPlayer(playerId);
            verifyActionClearance(player, "stand");
            dealer.handleStand(player);
        } finally {
            unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "stand", " player:" + playerId + " table:" + getId());
        }
    }

    public void lock() {
        getLock().lock();
    }

    @Override
    public void hit(UUID playerId) {
        LOGGER.entering(getClass().getName(), "hit ", " player:" + playerId + " table:" + getId());
        try {
            lock();// Lock releases immediately if player is not in turn
            BlackjackPlayer_ player = getPlayer(playerId);
            verifyActionClearance(player, "hit");
            dealer.handleHit(player);
        } finally {
            unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "hit", " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void split(UUID playerId) {
        LOGGER.entering(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
        try {
            lock();
            BlackjackPlayer_ player = getPlayer(playerId);
            verifyActionClearance(player, "split");
            dealer.handleSplit(player);
        } finally {
            unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
        }
    }

    @Override
    public void doubleDown(UUID playerId) {
        LOGGER.entering(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
        try {
            lock();
            BlackjackPlayer_ player = getPlayer(playerId);
            verifyActionClearance(player, "doubleDown");
            dealer.handleDoubleDown(player);
        } finally {
            unlockPlayerInTurn();
            LOGGER.exiting(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
        }
    }

    public void unlockPlayerInTurn() {
        if (getLock().isHeldByCurrentThread())
            getLock().unlock();
    }

    private void verifyActionClearance(CardPlayer player, String actionName) {
        if (!isPlayerAllowedToMakeAction(player)) {
            LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: '" + actionName + "' playerInTurn:" + getActivePlayer() + " phase: " + getGamePhase() + " in table:" + this);
            throw new IllegalPlayerActionException(actionName + " not allowed for player:" + player);
        }
    }

    private boolean isPlayerAllowedToMakeAction(CardPlayer player) {
        return isActivePlayer(player) && isGamePhase(BlackjackGamePhase.PLAY) && player.canAct();
    }

    public BlackjackHand getDealerHand() {
        return dealer.getHand();
    }


    @Override
    public Dealer getDealer() {
        return this.dealer;
    }

    @Override
    public BlackjackGamePhase getGamePhase() {
        return (BlackjackGamePhase) phasePath.getPhase();
    }


}
