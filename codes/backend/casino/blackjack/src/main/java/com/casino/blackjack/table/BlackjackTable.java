package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.external.IBlackjackTable;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;

public final class BlackjackTable extends SeatedTable implements IBlackjackTable {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;

	public BlackjackTable(Status initialStatus, Thresholds thresholds, UUID id) {
		super(initialStatus, thresholds, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, thresholds);
	}

//
	@Override
	public boolean trySeat(int seatNumber, ICasinoPlayer player) {
		boolean gotSeat = super.trySeat(seatNumber, player);
		if (gotSeat)
			dealer.handleNewPlayer(player);
		return gotSeat;
	}

	@Override
	public void placeStartingBet(ICasinoPlayer player, BigDecimal bet) {
		// Replaces previous bet if bet is allowed. UI handles usability
		try {
			if (isGamePhase(GamePhase.BET))
				dealer.handlePlayerBet(player, bet);
			else {
				LOGGER.severe("Starting bet is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("placeStartingBet is not allowed:" + player + " bet:" + bet.toString(), 16);
			}
		} finally {
			LOGGER.exiting(getClass().getName(), "placeStartingBet");
		}
	}

	@Override
	public void insure(BlackjackPlayer player) {
		try {
			if (isGamePhase(GamePhase.INSURE))
				dealer.insure(player);
			else {
				LOGGER.severe("insuring is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("insuring is not allowed:" + getGamePhase() + " table:" + this + " player:" + player, 44);// 44=number for JUnit test to catch
			}
		} finally {
			LOGGER.exiting(getClass().getName(), "insure");
		}
	}

	@Override
	public void stand(BlackjackPlayer player) {
		try {
			tryLockingPlayerInTurn(player, "stand");
			verifyActionClearance(player, "stand");// Lock releases immediately if player is not in turn
			dealer.stand(player);
			dealer.updateTableActor();
		} finally {
			completeAction("stand");
		}
	}

	private void tryLockingPlayerInTurn(BlackjackPlayer player, String method) {
		if (!getPlayerInTurnLock().tryLock()) {
			throw new ConcurrentModificationException(method + " called but no lock " + player);
		}
	}

	@Override
	public void takeCard(BlackjackPlayer player) {
		try {
			tryLockingPlayerInTurn(player, "takeCard");
			verifyActionClearance(player, "takeCard");// Lock releases immediately if player is not in turn
			dealer.addPlayerCard(player);
			dealer.updateTableActor();
		} finally {
			completeAction("takeCard");
		}
	}

	@Override
	public void splitStartingHand(BlackjackPlayer player) {
		try {
			tryLockingPlayerInTurn(player, "splitStartingHand");
			verifyActionClearance(player, "splitStartingHand");
			dealer.handleSplit(player);
		} finally {
			completeAction("splitStartingHand");
		}
	}

	@Override
	public void doubleDown(BlackjackPlayer player) {
		try {
			tryLockingPlayerInTurn(player, "doubleDown");
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			dealer.updateTableActor();
		} finally {
			completeAction("doubleDown");
		}
	}

	private void completeAction(String actionName) {
		if (getPlayerInTurnLock().isHeldByCurrentThread())
			getPlayerInTurnLock().unlock();
		LOGGER.exiting(getClass().getName(), actionName);
	}

	private void verifyActionClearance(ICasinoPlayer player, String actionName) {
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: " + actionName + " in turn:" + getPlayerInTurn() + " phase: " + getGamePhase() + " in table:" + this);
			throw new IllegalPlayerActionException(actionName + " not allowed for player:" + player, 14);
		}
	}

	private boolean isPlayerAllowedToMakeAction(ICasinoPlayer player) {
		return isPlayerInTurn(player) && isGamePhase(GamePhase.PLAY) && player.canAct();
	}

	@Override
	public void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerTimeout(ICasinoPlayer timedOutPlayer) {
		LOGGER.info("Player timedOut:" + timedOutPlayer);
		try {
			getPlayerInTurnLock().lock();
			if (timedOutPlayer.hasActiveHand())
				timedOutPlayer.getActiveHand().complete();
			if (isPlayerInTurn(timedOutPlayer))
				dealer.updateTableActor();
		} finally {
			if (getPlayerInTurnLock().isHeldByCurrentThread())
				getPlayerInTurnLock().unlock();
		}
	}

	@Override
	public void onBetPhaseEnd() {
		LOGGER.entering(getClass().getName(), "onBetPhaseEnd");
		try {
			if (!isGamePhase(GamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", getGamePhase(), GamePhase.BET);
			getPlayerInTurnLock().lock();
			dealer.finalizeBetPhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onBetPhaseEnd");
		}
	}

	@Override
	public void onInsurancePhaseEnd() {
		LOGGER.entering(getClass().getName(), "onInsurancePhaseEnd");
		try {
			if (!isGamePhase(GamePhase.INSURE))
				throw new IllegalPhaseException("GamePhase is not what is expected on insurancePhaseEnd", getGamePhase(), GamePhase.INSURE);
			getPlayerInTurnLock().lock();
			dealer.finalizeInsurancePhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onInsurancePhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onInsurancePhaseEnd");
		}
	}

	public Thresholds getThresholds() {
		return super.getThresholds();
	}

	@Override
	public void onTableClose() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPlayerTurnTime() {
		return getThresholds().playerHandTime();
	}

}
