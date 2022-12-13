package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.ext.BlackjackTableProxy;
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
import com.casino.common.user.Bridge;

public final class BlackjackTable extends SeatedTable implements BlackjackTableProxy {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;

	public BlackjackTable(Status initialStatus, Thresholds thresholds, UUID id) {
		super(initialStatus, thresholds, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, thresholds);
	}

//
	@Override
	public boolean join(Bridge bridge, int seatNumber) {
		BlackjackPlayer player = new BlackjackPlayer(bridge, this);
		boolean gotSeat = super.trySeat(seatNumber, player);
		if (gotSeat)
			dealer.handleNewPlayer(player);
		System.out.println("player Joined:" + player);
		return gotSeat;
	}

	@Override
	public void bet(UUID playerId, BigDecimal bet) {
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
			LOGGER.exiting(getClass().getName(), "placeStartingBet");
		}
	}

	@Override
	public void insure(UUID playerId) {
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
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
	public void stand(UUID playerId) {
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn(player, "stand");// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "stand");
			dealer.stand(player);
			dealer.updateTableActor();
		} finally {
			unlockPlayerInTurn("stand");
		}
	}

	private void lockPlayerInTurn(BlackjackPlayer player, String method) {
		getPlayerInTurnLock().lock();
//		if (!getPlayerInTurnLock().tryLock()) {
//			throw new ConcurrentModificationException(method + " called but no lock " + player);
//		}
	}

	@Override
	public void hit(UUID playerId) {
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn(player, "takeCard");// Lock releases immediately if player is not in turn
			verifyActionClearance(player, "takeCard");
			dealer.addPlayerCard(player);
			dealer.updateTableActor();
		} finally {
			unlockPlayerInTurn("takeCard");
		}
	}

	@Override
	public void split(UUID playerId) {
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn(player, "splitStartingHand");
			verifyActionClearance(player, "splitStartingHand");
			dealer.handleSplit(player);
		} finally {
			unlockPlayerInTurn("splitStartingHand");
		}
	}

	@Override
	public void doubleDown(UUID playerId) {
		try {
			BlackjackPlayer player = (BlackjackPlayer) getPlayer(playerId);
			lockPlayerInTurn(player, "doubleDown");
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			dealer.updateTableActor();
		} finally {
			unlockPlayerInTurn("doubleDown");
		}
	}

	private void unlockPlayerInTurn(String actionName) {
		if (getPlayerInTurnLock().isHeldByCurrentThread())
			getPlayerInTurnLock().unlock();
		LOGGER.exiting(getClass().getName(), actionName);
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
		LOGGER.entering(getClass().getName(), "onPlayerTimeout");
		try {
			getPlayerInTurnLock().lock();
			if (!isPlayerInTurn(timedOutPlayer)) {
				return;
			}
			dealer.autoplay(timedOutPlayer);
			dealer.updateTableActor();
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onPlayerTimeout");
		}
	}

	@Override
	public void onBetPhaseEnd() {
		LOGGER.entering(getClass().getName(), "onBetPhaseEnd");
		try {
			getPlayerInTurnLock().lock();
			if (!isGamePhase(GamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", getGamePhase(), GamePhase.BET);
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

	public void onInsurancePhaseEnd() {
		LOGGER.entering(getClass().getName(), "onInsurancePhaseEnd");
		try {
			getPlayerInTurnLock().lock();
			if (!isGamePhase(GamePhase.INSURE))
				throw new IllegalPhaseException("GamePhase is not what is expected on insurancePhaseEnd", getGamePhase(), GamePhase.INSURE);
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
