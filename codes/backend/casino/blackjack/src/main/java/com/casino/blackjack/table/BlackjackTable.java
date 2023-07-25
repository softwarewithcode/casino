package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.export.BlackjackAPI;
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.game.BlackjackInitData;
import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.structure.Seat;
import com.casino.common.table.structure.SeatedTable;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableData;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 * 
 */
@JsonIgnoreProperties(value = { "dealer" /* explicitly ignoring dealer for not exposing deck to UI */ })
@JsonIncludeProperties(value = { "type", "id", "activePlayer", "gamePhase", "watcherCount", "seats", "players", "counterTime", "tableCard", "dealerHand" })
public final class BlackjackTable extends SeatedTable<BlackjackPlayer> implements BlackjackAPI {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;
	private final TableCard tableCard;

	public BlackjackTable(TableData initData, BlackjackInitData blackjackInitData) {
		super(initData);
		this.dealer = new BlackjackDealer(this, blackjackInitData);
		this.tableCard = new TableCard(initData, blackjackInitData);
	}

	@Override
	public boolean join(User user, String seatNumber) {
		LOGGER.entering(getClass().getName(), "join", getId());
		try {
			BlackjackPlayer player = new BlackjackPlayer(user, this);
			Optional<Seat<BlackjackPlayer>> seatOptional = super.join(seatNumber, player);
			if (seatOptional.isPresent()) {
				player.setSeatNumber(seatOptional.get().getNumber());
				player.setStatus(PlayerStatus.ACTIVE);
				dealer.onPlayerArrival(player);
				removeWatcher(user.userId());
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
			BlackjackPlayer player = getPlayer(playerId);
			if (isGamePhase(BlackjackGamePhase.BET))
				dealer.updatePlayerBet(player, bet);
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
			BlackjackPlayer player = getPlayer(playerId);
			if (isGamePhase(BlackjackGamePhase.INSURE))
				dealer.insure(player);
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
			BlackjackPlayer player = getPlayer(playerId);
			lock();// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "stand");
			dealer.stand(player);
			dealer.updateActorAndNotify();
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
			BlackjackPlayer player = getPlayer(playerId);
			lock();// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "hit");
			dealer.hit(player);
			dealer.updateActorAndNotify();
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "hit", " player:" + playerId + " table:" + getId());
		}
	}

	@Override
	public void split(UUID playerId) {
		LOGGER.entering(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = getPlayer(playerId);
			lock();
			verifyActionClearance(player, "split");
			dealer.handleSplit(player);
			dealer.updateActorAndNotify();
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
		}
	}

	@Override
	public void doubleDown(UUID playerId) {
		LOGGER.entering(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = getPlayer(playerId);
			lock();
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			dealer.updateActorAndNotify();
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
		}
	}

	public void unlockPlayerInTurn() {
		if (getLock().isHeldByCurrentThread())
			getLock().unlock();
	}

	private void verifyActionClearance(ICasinoPlayer player, String actionName) {
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: '" + actionName + "' playerInTurn:" + getActivePlayer() + " phase: " + getGamePhase() + " in table:" + this);
			throw new IllegalPlayerActionException(actionName + " not allowed for player:" + player);
		}
	}

	private boolean isPlayerAllowedToMakeAction(ICasinoPlayer player) {
		return isActivePlayer(player) && isGamePhase(BlackjackGamePhase.PLAY) && player.canAct();
	}

	@Override
	public void leave(UUID playerOrWatcherId) {
		LOGGER.entering(getClass().getName(), "leave", " playerOrWatcherId:" + playerOrWatcherId + " table:" + getId());
		BlackjackPlayer leavingPlayer = null;
		try {
			leavingPlayer = getPlayer(playerOrWatcherId);
			if (leavingPlayer == null) {
				removeWatcher(playerOrWatcherId);
				return;
			}
			lock();
			dealer.onPlayerLeave(leavingPlayer);
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "leave", " playerOrWatcherId:" + leavingPlayer + " table:" + getId());
		}
	}

	public BlackjackHand getDealerHand() {
		return dealer.getHand();
	}

	@Override
	public synchronized void onClose() {
		setStatus(TableStatus.CLOSING);
		super.onClose();
	}

	@Override
	public void watch(User user) {
		BlackjackPlayer player = new BlackjackPlayer(user, this);
		if (getPlayer(user.userId()) != null) {
			LOGGER.fine("User " + user.userName() + " is already playing in table:" + this);
			return;
		}
		boolean joined = super.joinAsWatcher(player);
		if (joined)
			dealer.onWatcherArrival(player);
	}

	@Override
	public void refresh(UUID playerId) {
		LOGGER.entering(getClass().getName(), "refresh table:" + getId() + " player:" + playerId);
		LOGGER.fine("table_refresh ");
		if (playerId == null)
			return;
		try {
//			Thread.ofVirtual().start(() -> {

			dealer.handlePlayerComeBack(getPlayer(playerId));
//			});
		} finally {
			LOGGER.exiting(getClass().getName(), playerId.toString());
		}
	}

	@Override
	public TableCard getTableCard() {
		List<Integer> seats = getSeats().stream().filter(seat -> !seat.hasPlayer()).map(Seat::getNumber).toList();
		tableCard.setAvailablePositions(seats);
		return tableCard;
	}

	public void updatePlayersToWatchers(boolean all) {
		super.updatePlayersToWatchers(all);
	}

	@Override
	public BlackjackDealer getDealer() {
		return this.dealer;
	}

	@Override
	public BlackjackGamePhase getGamePhase() {
		return (BlackjackGamePhase) phasePath.getPhase();
	}

}
