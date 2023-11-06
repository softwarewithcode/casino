package com.casino.roulette.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.runner.CasinoMode;
import com.casino.roulette.export.BetType;

public class PositionTests extends RouletteBaseTests {

	@Test
	public void position37WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 37, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1140.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1160.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("20.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
	}

	@Test
	public void position38WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 38, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1320.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
	}

	@Test
	public void position39WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 39, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1220.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalOnTable());
		assertEquals(new BigDecimal("1200.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
	}

	@Test
	public void position40WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 40, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1320.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("20.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
	}

	@Test
	public void position41WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 41, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1200.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
	}

	@Test
	public void position42WinsWhenSpinResultIsZero() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "0");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1320.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position200WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 200, TWENTY);
		assertEquals(BetType.FIRST_DOZEN, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1020.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void position72WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 72, TWENTY);
		assertEquals(BetType.DOUBLE_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("1320.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("20.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
	}

	@Test
	public void position64WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 64, TWENTY);
		assertEquals(BetType.SIX_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1100.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("1080.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("20.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
	}

	@Test
	public void position68WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 68, TWENTY);
		assertEquals(BetType.QUADRUPLE_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1160.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position70WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 70, TWENTY);
		assertEquals(BetType.TRIPLE_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1220.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("1200.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void position78WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 78, TWENTY);
		assertEquals(BetType.DOUBLE_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1340.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("1320.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void position147WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 147, TWENTY);
		assertEquals(BetType.THIRD_COLUMN, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1040.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position12WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 12, TWENTY);
		assertEquals(BetType.SINGLE_NUMBER, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1700.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position203WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 203, TWENTY);
		assertEquals(BetType.FIRST_HALF, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1020.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position204WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 204, TWENTY);
		assertEquals(BetType.EVEN, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1020.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position205WinsWhenSpinResultIsTwelve() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 205, TWENTY);
		assertEquals(BetType.RED, multiPlayerTable.getPlayer(usr.userId()).getBets().get(0).getType());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1020.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("1000.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void position199_and_209_throwExceptionsAsTheyDoNotExist() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12");
		multiPlayerTable.join(usr);
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), 199, TWENTY));
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), 209, TWENTY));
		assertEquals(THOUSAND, multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(THOUSAND, multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void position37DoesNotWinWhenSpinResultIsFour() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("980.00"), multiPlayerTable.getPlayer(usr.userId()).getCurrentBalance());
	}

	@Test
	public void positionBetExceedsMaxBetAndExceptionIsThrown() {
		multiPlayerTable.join(usr2);
		multiPlayerTable.bet(usr2.getId(), 42, new BigDecimal("4999.00"));
		multiPlayerTable.bet(usr2.getId(), 42, new BigDecimal("1.00"));
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.bet(usr2.getId(), 42, new BigDecimal("0.01")));
		waitSpinTime(multiPlayerTable);
		assertEquals(new BigDecimal("45000.00"), multiPlayerTable.getPlayer(usr2.userId()).getCurrentBalance());
	}

	@Test
	public void positionBetExceedsMaxBetAndExceptionIsThrown2() {
		multiPlayerTable.join(usr2);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.bet(usr2.getId(), 42, new BigDecimal("5000.01")));
	}

	@Test
	public void positionBetIsNotEnoughAndExceptionIsThrown() {
		multiPlayerTable.join(usr2);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.bet(usr2.getId(), 42, new BigDecimal("4.99")));
		waitSpinTime(multiPlayerTable);
		assertEquals(new BigDecimal("50000.00"), multiPlayerTable.getPlayer(usr2.userId()).getTotalOnTable());
	}

	@Test
	public void betAmountUnderMinimumIsAllowedIfPositionBetAmountIsOverMinimum() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("5.0"));
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("0.01"));
		assertEquals(new BigDecimal("5.01"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(new BigDecimal("0.00"), multiPlayerTable.getPlayer(usr.userId()).getTotalBet());
	}



	@Test
	public void repeatingBetsWithoutSufficentFundsThrowsException() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "4");
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("500.01"));
		waitSpinTime(multiPlayerTable);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.repeatLastBets(usr.getId()));
	}

	@Test
	public void repeatingBetsWithoutSufficentFundsThrowsException2() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("250.0"));
		multiPlayerTable.bet(usr.getId(), 43, new BigDecimal("250.01"));
		waitSpinTime(multiPlayerTable);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.repeatLastBets(usr.getId()));
	}

	@Test
	public void repeatingPreviousRoundBetsIsOk() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("40.00"));
		multiPlayerTable.bet(usr.getId(), 43, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 44, new BigDecimal("44.00"));
		multiPlayerTable.bet(usr.getId(), 42, new BigDecimal("3.09"));
		waitForRoundToBeCompleted(multiPlayerTable);
		multiPlayerTable.repeatLastBets(usr.getId());
		assertEquals(new BigDecimal("131.09"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(4, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
	}

	@Test
	public void tryingToRepeatPreviousBetsThrowExceptionWhenNoPreviousBetsAreSet() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.repeatLastBets(usr.getId()));
	}

	@Test
	public void callingRepeatBetsWithoutAnyBetThrowsException() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.repeatLastBets(usr.getId()));
	}

	@Test
	public void repeatingLastBetsIsPossibleAfterRemovingBets() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 4, new BigDecimal("40.00"));
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(new BigDecimal("2360.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		multiPlayerTable.removeBets(usr.getId(), true);
		assertEquals(new BigDecimal("0.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		multiPlayerTable.repeatLastBets(usr.getId());
		assertEquals(new BigDecimal("40.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		multiPlayerTable.removeBets(usr.getId(), false);
		assertEquals(new BigDecimal("0.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		multiPlayerTable.repeatLastBets(usr.getId());
		assertEquals(new BigDecimal("40.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
	}

}
