package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetValues;
import com.casino.common.cards.IHand;
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

	@Override
	public boolean trySeat(int seatNumber, ICasinoPlayer player) {
		boolean gotSeat = super.trySeat(seatNumber, player);
		if (gotSeat) {
			dealer.handleNewPlayer(player);
		}
		return gotSeat;
	}

	public void placeInitialBet(ICasinoPlayer player, BigDecimal bet) {
		dealer.handlePlayerBet(player, bet);
	}

	public void takeCard(ICasinoPlayer player, IHand hand) {

	}

	public void splitHand(ICasinoPlayer player) {
		// One time split in basic blackjack

	}

	public void doubleInitialBet(ICasinoPlayer player) {

	}

	@Override
	public void onTimeout(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerTimeout(ICasinoPlayer player) {
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

	@Override
	public void onTableClose() {
		// TODO Auto-generated method stub

	}

	private boolean canProceedToPlayPhase() {
		return isGamePhase(GamePhase.BET) && lock.tryLock();
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
