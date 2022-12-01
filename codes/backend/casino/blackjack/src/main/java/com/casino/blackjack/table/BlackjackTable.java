package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetValues;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePathFactory;

public final class BlackjackTable extends SeatedTable {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTable.class.getName());
	private final BlackjackDealer dealer;
	private ReentrantLock betPhaseLock;
	private ReentrantLock playerInTurnLock;

	public BlackjackTable(Status initialStatus, BetValues betValues, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betValues, playerLimit, type, seats, id, PhasePathFactory.buildBlackjackPath());
		this.dealer = new BlackjackDealer(this, new BetInfo(betValues));
		betPhaseLock = new ReentrantLock(true);
		playerInTurnLock = new ReentrantLock(true);
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
		LOGGER.entering(getClass().getName(), "placeInitialBet:" + this + " player:" + player);
		// Replaces previous bet. UI handles usability
		try {
			if (isGamePhase(GamePhase.BET))
				dealer.handlePlayerBet(player, bet);
			else {
				LOGGER.info("Initial bet is not accepted due to the current gamePhase:" + getGamePhase() + " table:" + this + " player:" + player);
				throw new IllegalBetException("Initial bet in wrong phase:" + this + " player:" + player + " bet:" + bet.toString(), 1);
			}
		} finally {
			LOGGER.exiting(getClass().getName(), "placeInitialBet");
		}
	}

	// stand = no more cards and change of turn if next player exist
	public void stand(ICasinoPlayer player) {
		LOGGER.entering(getClass().getName(), "stand:" + this + " player:" + player);
		if (!isPlayerAllowedToPlay(player)) {
			// Timer might have run out or unauthorized call
			LOGGER.log(Level.SEVERE, " Player not in turn: " + player + " called stand:" + this);
			return;
		}
		try {
			if (!playerInTurnLock.tryLock()) {
				dealer.changeTurn();
				return;
			}
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread())
				playerInTurnLock.unlock();
			LOGGER.exiting(getClass().getName(), "stand");
		}
	}

	public void takeCard(ICasinoPlayer player) {
		LOGGER.entering(getClass().getName(), "takeCard:" + this + " player:" + player);
		if (!isPlayerAllowedToPlay(player)) {
			// Timer might have run out or unauthorized call
			LOGGER.log(Level.SEVERE, " Player not in turn: " + player + " called takeCard:" + this);
			return;
		}
		try {
			if (!playerInTurnLock.tryLock()) {
				LOGGER.log(Level.INFO, " Player -takeCard " + player + " tried to take card in table:" + this);
				dealer.addCard(player);
				return;
			}
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread())
				playerInTurnLock.unlock();
			LOGGER.exiting(getClass().getName(), "takeCard");
		}
	}

	private boolean isPlayerAllowedToPlay(ICasinoPlayer player) {
		return isPlayerInTurn(player) && isGamePhase(GamePhase.PLAY);
	}

	public void splitHand(ICasinoPlayer player) {
		// One time split in basic blackjack if two similar cards from initial deal

	}

	public void doubleHandBet(ICasinoPlayer player) {
		// If player get 9,10,11 in initial deal player is able to double the bet but
		// with only one card
	}

	@Override
	public void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerTimeout(ICasinoPlayer timedOutPlayer) {
		LOGGER.info("Player timedOut:" + timedOutPlayer);
		try {
			if (!playerInTurnLock.tryLock()) {
				return;
			}
			timedOutPlayer.setStatus(com.casino.common.player.Status.SIT_OUT);
			if (isPlayerInTurn(timedOutPlayer)) {
				dealer.changeTurn();
			}
		} finally {
			if (playerInTurnLock.isHeldByCurrentThread()) {
				playerInTurnLock.unlock();
			}
		}
	}

	@Override
	public void onBetPhaseEnd() {
		try {
			if (!canProceedToPlayPhase()) {
				throw new IllegalPhaseException("GamePhase is not correct or lock was not acquired", getGamePhase(), GamePhase.BET);
			}
			dealer.finalizeBetPhase();
			if (dealer.dealInitialCards()) {
				updateGamePhase(GamePhase.PLAY);
				dealer.updatePlayerInTurn();
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
	public BetInfo getBetInfo() {
		return dealer.getBetInfo();
	}

	@Override
	public String toString() {
		return "BlackjackTable [dealer=" + dealer + ", phase=" + getGamePhase() + "]";
	}

	@Override
	public int getPlayerTurnTime() {
		return 20;
	}

}
