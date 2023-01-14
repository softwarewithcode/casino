package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.Seat;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableInitData;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;
import com.casino.common.user.Bridge;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 * 
 */
@JsonIgnoreProperties(value = { "dealer" /* explicitly ignoring dealer for not exposing deck to UI */ })
@JsonIncludeProperties(value = { "type", "id", "language", "playerInTurn", "gamePhase", "watcherCount", "seats", "players", "counterTime", "tableCard", "dealerHand" })
public final class BlackjackTable extends SeatedTable implements IBlackjackTable {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;

	public BlackjackTable(Status initialStatus, TableInitData initData) {
		super(initialStatus, initData, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this);
	}

	@Override
	public boolean join(Bridge bridge, String seatNmbr) {
		LOGGER.entering(getClass().getName(), "join", getId());
		try {
			Integer seatNumber = seatNmbr != null ? Integer.parseInt(seatNmbr) : null;
			BlackjackPlayer player = new BlackjackPlayer(bridge, this);
			Optional<Seat> seatOptional = trySeat(seatNumber, player);
			if (seatOptional.isEmpty()) {
				return false;
			}
			player.setStatus(com.casino.common.player.PlayerStatus.ACTIVE);
			player.setSeatNumber(seatOptional.get().getNumber());
			super.removeWatcher(player.getId());
			dealer.onPlayerArrival(player);
			return true;
		} finally {
			LOGGER.exiting(getClass().getName(), "join" + " number:" + seatNmbr + " bridge:" + bridge + " tableId:" + getId());
		}
	}

	@Override
	public void bet(UUID playerId, BigDecimal bet) {
		LOGGER.entering(getClass().getName(), "bet", bet + " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = getPlayer(playerId);
			if (isGamePhase(GamePhase.BET))
				dealer.updatePlayerBet(player, bet);
			else {
				LOGGER.severe("Starting bet is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("placeStartingBet is not allowed:" + player + " bet:" + bet.toString(), 16);
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
			if (isGamePhase(GamePhase.INSURE))
				dealer.insure(player);
			else {
				LOGGER.severe("insuring is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("insuring is not allowed in phase: " + getGamePhase() + " table:" + this + " player:" + player, 44);
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
			lockPlayerInTurn();// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "stand");
			dealer.stand(player);
			dealer.updateActorAndNotify();
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "stand", " player:" + playerId + " table:" + getId());
		}
	}

	private void lockPlayerInTurn() {
		getPlayerInTurnLock().lock();
	}

	@Override
	public void hit(UUID playerId) {
		LOGGER.entering(getClass().getName(), "hit ", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = getPlayer(playerId);
			lockPlayerInTurn();// Lock releases immediately if player is not in turn
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
			lockPlayerInTurn();
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
			lockPlayerInTurn();
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			dealer.updateActorAndNotify();
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
		}
	}

	private void unlockPlayerInTurn() {
		if (getPlayerInTurnLock().isHeldByCurrentThread())
			getPlayerInTurnLock().unlock();
	}

	private void verifyActionClearance(ICasinoPlayer player, String actionName) {
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: '" + actionName + "' playerInTurn:" + getPlayerInTurn() + " phase: " + getGamePhase() + " in table:" + this);
			throw new IllegalPlayerActionException(actionName + " not allowed for player:" + player, 14);
		}
	}

	private boolean isPlayerAllowedToMakeAction(ICasinoPlayer player) {
		return isPlayerInTurn(player) && isGamePhase(GamePhase.PLAY) && player.canAct();
	}

	@Override
	public void onPlayerLeave(UUID playerId) {
		LOGGER.entering(getClass().getName(), "onPlayerLeave", " leavingPlayerId:" + playerId + " table:" + getId());
		BlackjackPlayer leavingPlayer = null;
		try {
			leavingPlayer = getPlayer(playerId);
			if (leavingPlayer == null)
				return;
			lockPlayerInTurn();
			dealer.onPlayerLeave(leavingPlayer);
		} finally {
			unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "onPlayerLeave", " leavingPlayer:" + leavingPlayer + " table:" + getId());
		}
	}

	@Override
	public void onPlayerTimeout(ICasinoPlayer timedOutPlayer) {
		LOGGER.entering(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer + " table:" + getId());
		try {
			lockPlayerInTurn();
			if (!isPlayerInTurn(timedOutPlayer)) {
				return;
			}
			dealer.handleTimedoutPlayer((BlackjackPlayer) timedOutPlayer);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer + " table:" + getId());
		}
	}

	@Override
	public void onBetPhaseEnd() {
		LOGGER.entering(getClass().getName(), "onBetPhaseEnd" + this.getId());
		try {
			lockPlayerInTurn();
			if (!isGamePhase(GamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", getGamePhase(), GamePhase.BET);
			dealer.finalizeBetPhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onBetPhaseEnd" + this.getId());
		}
	}

	public void onInsurancePhaseEnd() {
		LOGGER.entering(getClass().getName(), "onInsurancePhaseEnd" + this.getId());
		try {
			lockPlayerInTurn();
			if (!isGamePhase(GamePhase.INSURE))
				throw new IllegalPhaseException("GamePhase is not what is expected on insurancePhaseEnd", getGamePhase(), GamePhase.INSURE);
			dealer.finalizeInsurancePhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onInsurancePhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onInsurancePhaseEnd" + this.getId());
		}
	}

	public IHand getDealerHand() {
		return dealer.getHand();
	}

	@Override
	public synchronized void onClose() {
		setStatus(Status.CLOSING);
		super.onClose();
		super.sanitizeAllSeats();
		super.getWatchers().clear();
		setStatus(Status.CLOSED);
	}

	@Override
	public int getPlayerTurnTime() {
		return getThresholds().playerTime();
	}

	@Override
	public void prepareNewRound() {
		dealer.prepareNewRound();
	}

	@Override
	public boolean watch(Bridge user) {
		BlackjackPlayer player = new BlackjackPlayer(user, null);
		if (getPlayer(user.userId()) != null) {
			LOGGER.info("User " + user.userName() + " is already playing in table:" + this.toString());
			return false;
		}
		boolean joined = super.joinAsWatcher(player);
		if (joined)
			dealer.onWatcherArrival(player);
		return joined;
	}

	@Override
	public void refresh(UUID playerId) {
		LOGGER.entering(getClass().getName(), "refresh table:" + getId() + " player:" + playerId);
		LOGGER.info("table_refresh ");
		if (playerId == null)
			return;
		try {
//			Thread.ofVirtual().start(() -> {
			dealer.sendStatusUpdate(getPlayer(playerId));
//			});
		} finally {
			LOGGER.exiting(getClass().getName(), playerId.toString());
		}
	}

	@Override
	public TableCard getTableCard() {
		TableCard card = super.getTableCard();
		List<Integer> seats = getSeats().stream().filter(seat -> !seat.hasPlayer()).map(Seat::getNumber).toList();
		card.setAvailablePositions(seats);
		return card;
	}

	public void updatePlayersToWatchers(boolean all) {
		super.updatePlayersToWatchers(all);
	}
}
