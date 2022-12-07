package com.casino.blackjack.table;

import java.math.BigDecimal;
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
	private final InsuranceInfo insuranceInfo;

	public BlackjackTable(Status initialStatus, BetThresholds betThresholds, PlayerRange playerLimit, Type type, int seats, UUID id, InsuranceInfo insuranceInfo) {
		super(initialStatus, betThresholds, playerLimit, type, seats, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, betThresholds);
		this.insuranceInfo = insuranceInfo;
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
	public void insure(BlackjackPlayer player) {
		try {
			if (isGamePhase(GamePhase.INSURE))
				dealer.insure(player);
			else {
				LOGGER.severe("insuring is not accepted:phase " + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("insuring is not allowed:" + getGamePhase() + " table:" + this + " player:" + player, 16);
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
			checkDealer();
		} finally {
			completeAction("stand");
		}
	}

	@Override
	public void takeCard(BlackjackPlayer player) {
		try {
			verifyActionClearance(player, "takeCard");
			dealer.handleAdditionalCard(player);
			checkDealer();
		} finally {
			completeAction("takeCard");
		}
	}

	@Override
	public void splitStartingHand(BlackjackPlayer player) {
		try {
			verifyActionClearance(player, "splitStartingHand");
			dealer.handleSplit(player);
		} finally {
			completeAction("splitStartingHand");
		}
	}

	private void checkDealer() {
		if (getPlayerInTurn().getActiveHand() == null)
			dealer.changeTurn();
		if (isDealerTurn())
			dealer.completeRound();
	}

	@Override
	public void doubleDown(BlackjackPlayer player) {
		try {
			verifyActionClearance(player, "doubleDown");
			dealer.doubleDown(player);
			checkDealer();
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
			LOGGER.log(Level.SEVERE, "Player:" + player + " not allowed to make action: " + actionName + " phase: " + getGamePhase() + " in table:" + this);
			throw new IllegalPlayerActionException(actionName + " not allowed for player:" + player, 14);
		}
		getPlayerInTurnLock().lock();
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
		LOGGER.entering(getClass().getName(), "onBetPhaseEnd");
		try {
			if (!isGamePhase(GamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", getGamePhase(), GamePhase.BET);
			getPlayerInTurnLock().lock();
			updateGamePhase(GamePhase.BETS_COMPLETED);
			dealer.finalizeBetPhase();
			if (!dealer.dealInitialCards()) {
				return;
			}
			dealer.updateStartingPlayer();
			if (dealer.hasStartingAce()) {
				dealer.startInsurancePhase();
				updateGamePhase(GamePhase.INSURE);
			} else {
				checkDealer();
				updateGamePhase(GamePhase.PLAY);
			}
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
			updateGamePhase(GamePhase.PLAY);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onInsurancePhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(this, "onBetPhaseEnd");
			updateGamePhase(GamePhase.ERROR);
		} finally {
			getPlayerInTurnLock().unlock();
			LOGGER.exiting(getClass().getName(), "onInsurancePhaseEnd");
		}
	}

	public InsuranceInfo getInsuranceInfo() {
		return insuranceInfo;
	}

	@Override
	public void onTableClose() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPlayerTurnTime() {
		return 20;
	}

}
