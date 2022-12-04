package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.external.IBlackjackTable;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.bet.BetThresholds;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;

public final class BlackjackTable extends SeatedTable implements IBlackjackTable {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;

	public BlackjackTable(Status initialStatus, BetThresholds betThresholds, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betThresholds, playerLimit, type, seats, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, betThresholds);

	}

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
	public void stand(BlackjackPlayer player) {
		try {
			verifyActionClearance(player, "stand");
			dealer.stand(player);
			if (player.getActiveHand() == null)
				dealer.changeTurn();
		} finally {
			completeAction("stand");
		}
	}

	@Override
	public void takeCard(ICasinoPlayer player) {
		try {
			verifyActionClearance(player, "takeCard");
			dealer.addCard(player);
		} finally {
			completeAction("takeCard");
		}
	}

	@Override
	public void doubleDown(BlackjackPlayer player) {
		try {
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
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
		LOGGER.entering(getClass().getName(), actionName, " player:" + player);
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, " Player not allowed to make action: " + actionName + " phase: " + getGamePhase() + " in table:" + this);
			throw new IllegalPlayerActionException("not allowed", 14);
		}
		if (!getPlayerInTurnLock().tryLock()) { // .lock()
			System.out.println("Owner:" + getPlayerInTurnLock() + "  running:" + Thread.currentThread());
			LOGGER.log(Level.INFO, player + "tried:" + actionName + " but lock could not be obtained. " + this);
			throw new ConcurrentModificationException("lock was not obtained");
		}
	}

	private boolean isPlayerAllowedToMakeAction(ICasinoPlayer player) {
		return isPlayerInTurn(player) && isGamePhase(GamePhase.PLAY);
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
			// would require UI action "I'm back" if set to SIT_OUT here
			// timedOutPlayer.setStatus(com.casino.common.player.Status.SIT_OUT);
			if (isPlayerInTurn(timedOutPlayer))
				dealer.changeTurn();
		} finally {
			if (getPlayerInTurnLock().isHeldByCurrentThread())
				getPlayerInTurnLock().unlock();
		}
	}

	@Override
	public void onBetPhaseEnd() {
		try {
			if (!isGamePhase(GamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not correct or lock was not acquired", getGamePhase(), GamePhase.BET);
			getPlayerInTurnLock().lock();
			dealer.finalizeBetPhase();
			if (dealer.dealInitialCards()) {
				dealer.updateStartingPlayer();
				updateGamePhase(GamePhase.PLAY);
			} else {
				System.out.println("Players sit out. Nobody has bet. No use case specification exist. Either automatically restart betPhase vs. waiting players to join.");
				// How long sit out player can reserve the seat?
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			LOGGER.log(Level.INFO, "onBetPhaseEnd:");
			getPlayerInTurnLock().unlock();
		}
	}

	@Override
	public void onTableClose() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPlayerTurnTime() {
		return 20;
	}

	@Override
	public void splitStartingHand(BlackjackPlayer player) {
		dealer.handleSplit(player);
	}

	@Override
	public void insure(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

}
