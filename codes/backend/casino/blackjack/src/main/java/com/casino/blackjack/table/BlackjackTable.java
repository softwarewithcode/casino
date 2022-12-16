package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.ext.BlackjackReverseProxy;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;
import com.casino.common.user.Bridge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BlackjackTable extends SeatedTable implements BlackjackReverseProxy {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	@JsonIgnore // Don't expose the dealer at all
	private final BlackjackDealer dealer;

	public BlackjackTable(Status initialStatus, Thresholds thresholds, UUID id) {
		super(initialStatus, thresholds, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, thresholds);
	}

	@Override
	public boolean join(Bridge bridge, String seatNumber) {
		LOGGER.entering(getClass().getName(), "join", getId());
		LOGGER.info("table_join " + bridge);
		boolean gotSeat = false;
		try {
			int seat = Integer.parseInt(seatNumber);
			BlackjackPlayer joinedPlayer = new BlackjackPlayer(bridge, this);
			gotSeat = super.trySeat(seat, joinedPlayer);
			if (gotSeat)
				dealer.onPlayerArrival(joinedPlayer);
			return gotSeat;
		} finally {
			LOGGER.exiting(getClass().getName(), "join" + " gotSeat:" + gotSeat + " number:" + seatNumber + " bridge:" + bridge + " tableId:" + getId());
		}
	}

	@Override
	public void bet(UUID playerId, BigDecimal bet) {
		LOGGER.entering(getClass().getName(), "bet", bet + " player:" + playerId + " table:" + getId());
		LOGGER.info("table_bet " + playerId + " bet:" + bet);
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			System.out.println("player bet:" + bet);
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
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			if (isGamePhase(GamePhase.INSURE))
				dealer.insure(player);
			else {
				LOGGER.severe("insuring is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("insuring is not allowed:" + getGamePhase() + " table:" + this + " player:" + player, 44);
			}
		} finally {
			LOGGER.exiting(getClass().getName(), "insure", " player:" + playerId + " table:" + getId());
		}
	}

	@Override
	public void stand(UUID playerId) {
		LOGGER.entering(getClass().getName(), "stand", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn();// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "stand");
			dealer.stand(player);
			dealer.finalizeAction(player);
		} finally {
			unlockPlayerInTurn("stand");
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
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn();// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "hit");
			dealer.addPlayerCard(player);
			dealer.finalizeAction(player);
		} finally {
			unlockPlayerInTurn("hit");
			LOGGER.exiting(getClass().getName(), "hit", " player:" + playerId + " table:" + getId());
		}
	}

	@Override
	public void split(UUID playerId) {
		LOGGER.entering(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn();
			verifyActionClearance(player, "split");
			dealer.handleSplit(player);
			dealer.finalizeAction(player);
		} finally {
			unlockPlayerInTurn("split");
			LOGGER.exiting(getClass().getName(), "split", " player:" + playerId + " table:" + getId());
		}
	}

	@Override
	public void doubleDown(UUID playerId) {
		LOGGER.entering(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn();
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			dealer.updateTableActor();
		} finally {
			unlockPlayerInTurn("doubleDown");
			LOGGER.exiting(getClass().getName(), "doubleDown", " player:" + playerId + " table:" + getId());
		}
	}

	private void unlockPlayerInTurn(String actionName) {
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
	public synchronized void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerTimeout(ICasinoPlayer timedOutPlayer) {
		LOGGER.entering(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer + " table:" + getId());
		try {
			lockPlayerInTurn();
			if (!isPlayerInTurn(timedOutPlayer)) {
				return;
			}
			dealer.autoplay(timedOutPlayer);
			dealer.updateTableActor();
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer + " table:" + getId());
		}
	}

	@Override
	public void onBetPhaseEnd() {
		LOGGER.entering(getClass().getName(), "onBetPhaseEnd" + this.getId());
		try {
			System.out.println("betphase has ended");
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

	@JsonProperty
	public IHand dealerHand() {
		return dealer.getHand();
	}

	public Thresholds getThresholds() {
		return super.getThresholds();
	}

	@Override
	public synchronized void onTableClose() {
		// TODO Auto-generated method stub
	}

	@Override
	public int getPlayerTurnTime() {
		return getThresholds().playerHandTime();
	}

	@Override
	public void prepareNewRound() {
		dealer.prepareNewRound();
	}

	@Override
	public boolean watch(Bridge user) {
		BlackjackPlayer player = new BlackjackPlayer(user, this);
		return super.joinAsWatcher(player);
	}
}
