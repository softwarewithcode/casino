package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
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
	private final ReentrantLock betPhaseLock;
	private final ReentrantLock playerInTurnLock;

	public BlackjackTable(Status initialStatus, BetThresholds betThresholds, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betThresholds, playerLimit, type, seats, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, betThresholds);
		betPhaseLock = new ReentrantLock(true);
		playerInTurnLock = new ReentrantLock(true);
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
		LOGGER.entering(getClass().getName(), "placeStartingBet:" + this + " player:" + player);
		// Replaces previous bet if bet is allowed. UI handles usability
		try {
			if (isGamePhase(GamePhase.BET))
				dealer.handlePlayerBet(player, bet);
			else {
				LOGGER.severe("Starting bet is not accepted:" + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalPlayerActionException("placeStartingBet is not allowed:" + player + " bet:" + bet.toString(), 16);
			}
		} finally {
			LOGGER.exiting(getClass().getName(), "placeStartingBet");
		}
	}

	@Override
	public void stand(BlackjackPlayer player) {
		LOGGER.entering(getClass().getName(), "stand:" + this + " player:" + player);
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, " Player called illegal stand: " + player + " table:" + this);
			throw new IllegalPlayerActionException("Player:" + player + " is not allowed to stand in table :", 15);
		}
		try {
			dealer.stand(player);
			if (player.getActiveHand() == null)
				dealer.changeTurn();
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread())
				playerInTurnLock.unlock();
			LOGGER.exiting(getClass().getName(), "stand");
		}
	}

	@Override
	public void takeCard(ICasinoPlayer player) {
		LOGGER.entering(getClass().getName(), "takeCard:" + this + " player:" + player);
		if (!isPlayerAllowedToMakeAction(player)) {
			LOGGER.log(Level.SEVERE, " Player not in turn: " + player + " called takeCard in table:" + this);
			throw new IllegalPlayerActionException("Player:" + player + " is not allowed to take card in table:", 14);
		}
		try {
			if (!playerInTurnLock.tryLock()) {
				LOGGER.log(Level.INFO, " Player -takeCard " + player + " tried to take card in table:" + this);
				return;
			}
			dealer.addCard(player);
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread())
				playerInTurnLock.unlock();
			LOGGER.exiting(getClass().getName(), "takeCard");
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
			playerInTurnLock.lock(); 
			// would require UI action "I'm back" if set to SIT_OUT here
			//timedOutPlayer.setStatus(com.casino.common.player.Status.SIT_OUT);
			if (isPlayerInTurn(timedOutPlayer))
				dealer.changeTurn();
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread())
				playerInTurnLock.unlock();
		}
	}

	@Override
	public void onBetPhaseEnd() {
		try {
			if (!canProceedToPlayPhase())
				throw new IllegalPhaseException("GamePhase is not correct or lock was not acquired", getGamePhase(), GamePhase.BET);
			dealer.finalizeBetPhase();
			if (dealer.dealInitialCards()) {
				updateGamePhase(GamePhase.PLAY);
				dealer.updateStartingPlayer();
			}
		} catch (IllegalPhaseException re) {
			LOGGER.log(Level.SEVERE, "betPhaseEnd dealer cannot deal:", re);
			throw re;
		} finally {
			LOGGER.log(Level.INFO, "onBetPhaseEnd, releasing lock holdCount:" + betPhaseLock.getHoldCount());
			betPhaseLock.unlock();
		}
	}

	@Override
	public void onTableClose() {
		// TODO Auto-generated method stub

	}

	private boolean canProceedToPlayPhase() {
		return isGamePhase(GamePhase.BET) && betPhaseLock.tryLock();
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
	public void doubleDown(BlackjackPlayer player) {
		LOGGER.entering(getClass().getName(), "doubleStartingBet:" + this + " player:" + player);
		try {
			if (!isPlayerAllowedToMakeAction(player)) {
				LOGGER.log(Level.SEVERE, " Player not allowed to doubleDown: " + player + " table:" + this);
				throw new IllegalPlayerActionException("doublingDown not allowed for:" + player + " phase:" + getGamePhase(), 13);
			}
			dealer.doubleDown(player);
		} finally {
			LOGGER.exiting(getClass().getName(), "doubleStartingBet");
		}
	}

	@Override
	public void insure(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

}
