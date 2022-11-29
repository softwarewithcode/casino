package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetValues;
import com.casino.common.common.IllegalBetException;
import com.casino.common.common.PlayerNotFoundException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTest extends BaseTest {

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		publicTable.join(blackjackPlayer);
		Assertions.assertEquals(1, publicTable.getWatchers().size());
		Assertions.assertEquals(0, publicTable.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		takeSeat(0, blackjackPlayer);
		Assertions.assertEquals(0, publicTable.getWatchers().size());
		Assertions.assertEquals(1, publicTable.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		takeSeat(0, blackjackPlayer);
		takeSeat(0, blackjackPlayer2);
		Assertions.assertEquals(blackjackPlayer, publicTable.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("2.5"));
		assertThrows(IllegalArgumentException.class, () -> {
			table.trySeat(0, blackjackPlayer);
		});
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void placingBetInWrongPhaseResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("50.0"));
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void emptyBetResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"));
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer, null);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void placingBetOverBalanceButWithinTableLimitsResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"));
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("50.001"));
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void placingBetToPlayerNotInTableResultsInException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"));
		table.trySeat(0, blackjackPlayer);
		PlayerNotFoundException exception = assertThrows(PlayerNotFoundException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer2, new BigDecimal("7.0"));
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void placingBetUnderTableMinimumResultsException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"));
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("4.99"));
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void placingBetOverTableMaximuResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"));
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("100.1"));
		});
		assertEquals(5, exception.getCode());
	}

	@Test
	public void placingAllowedBetSetsTheBetForPlayer() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"));
		table.trySeat(0, blackjackPlayer);
		table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("49.9"));
		assertEquals(blackjackPlayer.getBet().toString(), "49.9");
	}

	@Test
	public void initialHandIsDealtAfterBetRoundHasEnded() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, 2, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"));
		table.trySeat(0, blackjackPlayer);
		table.getDealer().handlePlayerBet(blackjackPlayer, new BigDecimal("49.9"));
		sleep(5, ChronoUnit.SECONDS);
		BlackjackPlayer b = (BlackjackPlayer) blackjackPlayer;
		assertEquals(2, b.getHands().get(0).getCards().size());
	}
}
