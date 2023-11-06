package com.casino.roulette.tests;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.runner.CasinoMode;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.casino.roulette.game.RouletteGamePhase;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveBetTests extends RouletteBaseTests {
	private static final Boolean REMOVE_PREVIOUS = false;
	private static final Boolean REMOVE_ALL = true;

	@Test
	public void removeBetsRemovesPreviousBet() {
		multiPlayerTable.join(usr);
		assertEquals(0, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.bet(usr.getId(), 36, TWENTY);
		assertEquals(1, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(0, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void removingPreviousBetUpdatesBalance() {
		multiPlayerTable.join(usr);
		assertEquals(new BigDecimal("1000.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.bet(usr.getId(), 36, TWENTY);
		assertEquals(new BigDecimal("980.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(new BigDecimal("1000.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(0, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void removingLastBetUpdatesBalance() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("123.79"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("59.33"));
		assertEquals(new BigDecimal("816.88"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(new BigDecimal("876.21"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(1, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void removingLastBetUpdatesBalance_2() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("123.79"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("59.33"));
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(THOUSAND, multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void removingLastBetUpdatesBalance_3() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("123.79"));
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("59.33"));
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("59.33"));
		assertEquals(3, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(2, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(1, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS);
		assertEquals(0, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS));
	}

	@Test
	public void removingPreviousBetWithoutBetThrowsException() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS));
	}

	@Test
	public void removingPreviousBetIsNotAllowedInGamePhases() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("123.79"));
		List<RouletteGamePhase> phases = Stream.of(RouletteGamePhase.values()).filter(phase -> phase != RouletteGamePhase.BET).toList();
		phases.forEach(phase -> {
			multiPlayerTable.updateGamePhase(phase);
			assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.removeBets(usr.getId(), REMOVE_PREVIOUS));
		});
	}

	@Test
	public void removingAllBetsIsNotAllowedInGamePhases() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("123.79"));
		List<RouletteGamePhase> phases = Stream.of(RouletteGamePhase.values()).filter(phase -> phase != RouletteGamePhase.BET).toList();
		phases.forEach(phase -> {
			multiPlayerTable.updateGamePhase(phase);
			assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.removeBets(usr.getId(), REMOVE_ALL));
		});
	}

	@Test
	public void removingAllBetsReturnsOriginalBalance() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("100.0"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("101.00"));
		assertEquals(new BigDecimal("799.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), REMOVE_ALL);
		assertEquals(THOUSAND, multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void removingBetsFromPositionUpdatesBalance() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("100.0"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("101.00"));
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("55.17"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("101.01"));
		assertEquals(new BigDecimal("642.82"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.removeBetsFromPosition(usr.getId(), 36);
		assertEquals(new BigDecimal("797.99"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void removingBetsFromPositionRemovesBets() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("100.0"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("101.00"));
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("55.17"));
		multiPlayerTable.bet(usr.getId(), 31, new BigDecimal("101.01"));
		assertEquals(4, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		multiPlayerTable.removeBetsFromPosition(usr.getId(), 36);
		assertEquals(2, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		assertEquals(31, multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getPosition());
        assertEquals(31,(multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getPosition()));
		assertEquals(2, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void position42WinsAndPlayerRemovesAllBetsIncludingWinningBetFromLastRound() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.removeBets(usr.getId(), true);
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
	}

	@Test
	public void position42WinsAndPlayerRemovesLastWinningBetFromLastRound() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
	}

	@Test
	public void playerBetsPosition37_38_and39_thenRemovesWinningBetsOneByOne() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 37, TWENTY);
		multiPlayerTable.bet(usr.getId(), 38, new BigDecimal("15.00"));
		multiPlayerTable.bet(usr.getId(), 39, new BigDecimal("9.00"));
		waitForRoundToBeCompleted(multiPlayerTable); // TODO does not match for next round with 		waitNextRoundStartQQQ(multiPlayerTable);
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1470.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("44.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1479.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1494.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
	}

	@Test
	public void playerBetsPosition37_38_and39_thenRemovesTwoLastWinningBetsAndBetsMore() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 37, TWENTY);
		multiPlayerTable.bet(usr.getId(), 38, new BigDecimal("15.00"));
		multiPlayerTable.bet(usr.getId(), 39, new BigDecimal("9.00"));
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1470.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1479.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1494.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("20.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
		multiPlayerTable.bet(usr.getId(), 38, new BigDecimal("432.00"));
		assertEquals(new BigDecimal("1514.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1062.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("452.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
		assertEquals(2, multiPlayerTable.getPlayer(usr.userId()).getBets().size());
	}

	@Test
	public void playerBetsPosition37_38_38_and39_thenRemovesPosition38BetsAndLastBetOnList() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 37, TWENTY);
		multiPlayerTable.bet(usr.getId(), 38, new BigDecimal("13.00"));
		multiPlayerTable.bet(usr.getId(), 38, new BigDecimal("17.00"));
		multiPlayerTable.bet(usr.getId(), 39, new BigDecimal("9.00"));
		assertEquals(new BigDecimal("941.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1000.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(new BigDecimal("1710.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1769.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		multiPlayerTable.removeBetsFromPosition(usr.getId(), 38);
		assertEquals(new BigDecimal("1740.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1769.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1749.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1769.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("1769.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1769.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
	}

	@Test
	public void removingBetsFromPositionThrowsExceptionWhenNoBetsExist() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.removeBetsFromPosition(usr.getId(), 36));
	}

	@Test
	public void removingLastFromPreviousRoundGetsRemoved() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "4");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("40.00"));
		multiPlayerTable.bet(usr.getId(), 43, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 44, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("3.09"));
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.repeatLastBets(usr.getId());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("128.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(3, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void allBetsAreRemovedAfterCopyingBetsFromPreviousRound() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "4");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("40.00"));
		multiPlayerTable.bet(usr.getId(), 43, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 44, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("3.09"));
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.repeatLastBets(usr.getId());
		multiPlayerTable.removeBets(usr.getId(), true);
		assertEquals(new BigDecimal("0.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(0, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void pileIsRemovedAfterCopyingBetsFromPreviousRound() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "4");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("40.00"));
		multiPlayerTable.bet(usr.getId(), 43, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 44, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("3.09"));
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.repeatLastBets(usr.getId());
		multiPlayerTable.removeBetsFromPosition(usr.getId(), 42);
		assertEquals(new BigDecimal("88.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(2, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}
}
