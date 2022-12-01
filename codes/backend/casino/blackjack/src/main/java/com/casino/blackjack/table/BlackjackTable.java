package com.casino.blackjack.table;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetValues;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;

public class BlackjackTable extends SeatedTable {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;
	private ReentrantLock lock;

	public BlackjackTable(Status initialStatus, BetValues betValues, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betValues, playerLimit, type, seats, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, new BetInfo(betValues));
		lock = new ReentrantLock(true);
	}

	@SuppressWarnings("exports")
	@Override
	public BlackjackDealer getDealer() {
		return dealer;
	}

	@Override
	public void onTimeout(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getComputerTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBetPhaseEnd() {
		try {
			if (!canProceedToPlayPhase()) {
				throw new IllegalPhaseException("GamePhase is not correct ot lock was not acquired", getGamePhase(), GamePhase.BET);
			}
			dealer.finalizeBetPhase();
			if (dealer.dealInitialCards()) {
				updateGamePhase(GamePhase.PLAY);
				dealer.updatePlayerInTurn();
			}
		} catch (IllegalPhaseException re) {
			LOGGER.log(Level.SEVERE, "betPhaseEnd dealer cannot deal:", re);
			super.updateGamePhase(null);
			dealer.returnBets();
			setStatus(Status.WAITING_PLAYERS);
		} finally {
			LOGGER.log(Level.INFO, "onBetPhaseEnd, releasing lock holdCount:" + lock.getHoldCount());
			lock.unlock();
		}
	}

	private boolean canProceedToPlayPhase() {
		return isGamePhase(GamePhase.BET) && lock.tryLock();
	}

	public void updatePlayerInTurn() {
		// TODO Auto-generated method stub
		System.out.println("Checking who's turn it is");
	}

	@Override
	public BetInfo getBetInfo() {
		return dealer.getBetInfo();
	}

	@Override
	public String toString() {
		return "BlackjackTable [dealer=" + dealer + ", phase=" + getGamePhase() + "]";
	}

}
