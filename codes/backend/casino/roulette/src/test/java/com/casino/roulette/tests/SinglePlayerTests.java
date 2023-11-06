package com.casino.roulette.tests;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.roulette.game.RouletteGamePhase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SinglePlayerTests extends RouletteBaseTests {
	@Test
	public void playFunctionSpinsWheel() {
		singlePlayerTable.join(usr);
		singlePlayerTable.bet(usr.getId(), 36, TWENTY);
		assertEquals(TWENTY, singlePlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(new BigDecimal("980.00"), singlePlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(RouletteGamePhase.BET, singlePlayerTable.getGamePhase());
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(4, singlePlayerTable.getWheel().getResultBoard().get(0).winningNumber());
		singlePlayerTable.bet(usr.getId(), 4, TWENTY);
		assertEquals(TWENTY, singlePlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(RouletteGamePhase.BET, singlePlayerTable.getGamePhase());
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("1660.00"), singlePlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("1680.00"), singlePlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(2, singlePlayerTable.getWheel().getResultBoard().size());
		assertEquals(4, singlePlayerTable.getWheel().getResultBoard().get(1).winningNumber());
		assertEquals(4, singlePlayerTable.getWheel().getResult().winningNumber());
	}

	@Test
	public void repeatedBetsAreExactlySameAsLastBets() {
		singlePlayerTable.join(usr);
		singlePlayerTable.bet(usr.getId(), 200, FIVE);
		singlePlayerTable.bet(usr.getId(), 201, FIVE);
		singlePlayerTable.bet(usr.getId(), 202, FIVE);
		singlePlayerTable.bet(usr.getId(), 203, FIVE);
		singlePlayerTable.bet(usr.getId(), 204, FIVE);
		singlePlayerTable.bet(usr.getId(), 205, new BigDecimal("9.53"));
		singlePlayerTable.bet(usr.getId(), 206, FIVE);
		singlePlayerTable.bet(usr.getId(), 207, FIVE);
		singlePlayerTable.bet(usr.getId(), 208, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(usr.getId()).getTotalBet());
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("995.47"), singlePlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		singlePlayerTable.repeatLastBets(usr.getId());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(new BigDecimal("995.47"), singlePlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("945.94"), singlePlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(9, singlePlayerTable.getPlayer(usr.getId()).getBets().size());
		assertEquals(200, singlePlayerTable.getPlayer(usr.getId()).getBets().get(0).getPosition());
		assertEquals(201, singlePlayerTable.getPlayer(usr.getId()).getBets().get(1).getPosition());
		assertEquals(202, singlePlayerTable.getPlayer(usr.getId()).getBets().get(2).getPosition());
		assertEquals(203, singlePlayerTable.getPlayer(usr.getId()).getBets().get(3).getPosition());
		assertEquals(204, singlePlayerTable.getPlayer(usr.getId()).getBets().get(4).getPosition());
		assertEquals(205, singlePlayerTable.getPlayer(usr.getId()).getBets().get(5).getPosition());
		assertEquals(206, singlePlayerTable.getPlayer(usr.getId()).getBets().get(6).getPosition());
		assertEquals(207, singlePlayerTable.getPlayer(usr.getId()).getBets().get(7).getPosition());
		assertEquals(208, singlePlayerTable.getPlayer(usr.getId()).getBets().get(8).getPosition());

		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(0).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(1).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(2).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(3).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(4).getAmount());
		assertEquals(new BigDecimal("9.53"), singlePlayerTable.getPlayer(usr.getId()).getBets().get(5).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(6).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(7).getAmount());
		assertEquals(FIVE, singlePlayerTable.getPlayer(usr.getId()).getBets().get(8).getAmount());
	}

	@Test
	public void repeatingLastBetsThrowsExceptionWithInsufficientFunds() {
		singlePlayerTable.join(singlePlayerUsr);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 200, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 201, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 203, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 204, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 205, new BigDecimal("9.53"));
		singlePlayerTable.bet(singlePlayerUsr.getId(), 206, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 207, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 208, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		singlePlayerTable.play(singlePlayerUsr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("45.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("20.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("25.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.repeatLastBets(singlePlayerUsr.getId()));
	}

	@Test
	public void gamePlayTestWithDifferentActions() {
		singlePlayerTable.join(singlePlayerUsr);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("0.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 200, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("5.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("44.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 201, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("10.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("39.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("15.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("34.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.play(singlePlayerUsr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("5.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("44.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.repeatLastBets(singlePlayerUsr.getId());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("15.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("34.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.play(singlePlayerUsr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("5.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("44.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.removeBets(singlePlayerUsr.getId(), true);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("0.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("5.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("44.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.removeBets(singlePlayerUsr.getId(), false);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("0.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 203, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 203, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 202, FIVE);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 204, FIVE);
		singlePlayerTable.removeBetsFromPosition(singlePlayerUsr.getId(), 202);
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("15.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("34.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 32, FIVE);
		singlePlayerTable.repeatLastBets(singlePlayerUsr.getId());
		assertEquals(new BigDecimal("49.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("15.00"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("34.53"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		assertEquals(200, singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getBets().get(0).getPosition());
		assertEquals(201, singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getBets().get(1).getPosition());
		assertEquals(202, singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getBets().get(2).getPosition());
		singlePlayerTable.bet(singlePlayerUsr.getId(), 4, new BigDecimal("5.87"));
		singlePlayerTable.play(singlePlayerUsr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("254.98"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("10.87"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("244.11"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.repeatLastBets(singlePlayerUsr.getId());
		assertEquals(new BigDecimal("254.98"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("20.87"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getTotalBet());
		assertEquals(new BigDecimal("234.11"), singlePlayerTable.getPlayer(singlePlayerUsr.getId()).getCurrentBalance());
		singlePlayerTable.play(singlePlayerUsr.getId(), singlePlayerTable.getWheel().getSpinId());
	}

	@Test
	public void previousSpinIdDoesNotAllowToSpin() {
		singlePlayerTable.join(singlePlayerUsr);
		singlePlayerTable.bet(singlePlayerUsr.getId(), 200, FIVE);
		UUID firstSpinId = singlePlayerTable.getWheel().getSpinId();
		singlePlayerTable.play(singlePlayerUsr.getId(), firstSpinId);
		assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.play(singlePlayerUsr.getId(), firstSpinId));
	}
	
	@Test
	public void playerCannotSpinWithoutBets() {
		singlePlayerTable.join(singlePlayerUsr);
		UUID firstSpinId = singlePlayerTable.getWheel().getSpinId();
		assertThrows(IllegalPlayerActionException.class, ()-> singlePlayerTable.play(singlePlayerUsr.getId(), firstSpinId));
	}
}
