package com.casino.roulette.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.casino.common.functions.Functions;
import com.casino.common.runner.CasinoMode;
import com.casino.roulette.bet.RouletteBet;
import com.casino.roulette.export.BetType;
import com.casino.roulette.persistence.RoundResult;
import com.casino.roulette.player.RoulettePlayer;
import com.casino.roulette.table.RouletteTable_;

public class ResultTests extends RouletteBaseTests {

	@Test
	public void roundResultIsCreatedAfterLosing() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 0, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		RoulettePlayer p = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(1, p.getRoundResults().size());
		RoundResult res = p.getRoundResults().get(0);
		assertEquals(0, res.playerResult().winningBets().size());
		assertEquals(1, res.playerResult().losingBets().size());
		assertEquals(TWENTY, res.playerResult().losingBets().get(0).getAmount());
		assertEquals(TWENTY,  res.playerResult().totalBets());
		assertEquals(BigDecimal.ZERO, res.playerResult().totalRemainingBets());
		assertEquals(BigDecimal.ZERO, res.playerResult().totalWinnings());
		assertEquals(1, res.spinResult().roundNumber());
		assertEquals(0, p.getBets().size());
		//assertDoesNotThrow(() -> UUID.fromString(res.id().toString()));
	}

	@Test
	public void roundResultIsCreatedAfterWinning() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		RoulettePlayer p = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(1, p.getRoundResults().size());
		RoundResult res = p.getRoundResults().get(0);
		assertEquals(1, res.playerResult().winningBets().size());
		assertEquals(0, res.playerResult().losingBets().size());
		assertEquals(TWENTY,res.playerResult().winningBets().get(0).getAmount());
		assertEquals(TWENTY,  res.playerResult().totalBets());
		assertEquals(TWENTY, res.playerResult().totalRemainingBets());
		assertEquals(new BigDecimal("700.00"), res.playerResult().totalWinnings());
		assertEquals(1, res.spinResult().roundNumber());
		assertEquals(1, p.getBets().size());
	}

	@Test
	public void secondRoundResultIsCreatedFromSecondRoundBetData() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "36");
		var player = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(0, player.getBets().size());
		betDefaults(multiPlayerTable);
		waitForRoundToBeCompleted(multiPlayerTable);
		
		assertEquals(2, player.getRoundResults().size());
		RoundResult res = player.getRoundResults().get(1);
		assertEquals(2, res.spinResult().roundNumber());
		res.playerResult().losingBets().forEach(System.out::println);
		assertEquals(5, res.playerResult().winningBets().size());
		assertEquals(7, res.playerResult().losingBets().size());
		assertEquals(new BigDecimal("140.00"), res.playerResult().totalWinnings());
		List<BigDecimal> losingBets = res.playerResult().losingBets().stream().map(RouletteBet::getAmount).toList();
		assertEquals(new BigDecimal("140.00"), Functions.calculateSum(losingBets));
		assertEquals(new BigDecimal("240.00"),  res.playerResult().totalBets());
		assertEquals(new BigDecimal("100.00"), res.playerResult().totalRemainingBets());
		assertEquals(BetType.THIRD_DOZEN, res.playerResult().winningBets().get(0).getType());
		assertEquals(BetType.EVEN, res.playerResult().winningBets().get(1).getType());
		assertEquals(BetType.RED,res.playerResult().winningBets().get(2).getType());
		assertEquals(BetType.SECOND_HALF, res.playerResult().winningBets().get(3).getType());
		assertEquals(BetType.THIRD_COLUMN, res.playerResult().winningBets().get(4).getType());

		assertEquals(BetType.FIRST_DOZEN, res.playerResult().losingBets().get(0).getType());
		assertEquals(BetType.SECOND_DOZEN, res.playerResult().losingBets().get(1).getType());
		assertEquals(BetType.FIRST_HALF, res.playerResult().losingBets().get(2).getType());
		assertEquals(BetType.BLACK, res.playerResult().losingBets().get(3).getType());
		assertEquals(BetType.ODD, res.playerResult().losingBets().get(4).getType());
		assertEquals(BetType.FIRST_COLUMN, res.playerResult().losingBets().get(5).getType());
		assertEquals(BetType.SECOND_COLUMN, res.playerResult().losingBets().get(6).getType());
	}

	@Test
	public void thirdRoundBetsArePlacedFromSecondRoundWinnings() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		waitForRoundToBeCompleted(multiPlayerTable);
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "36");
		betDefaults(multiPlayerTable);
		waitForRoundToBeCompleted(multiPlayerTable);
		var player = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(5, player.getBets().size());
		assertEquals(new BigDecimal("100.00"), player.getTotalBet());
		assertEquals(new BigDecimal("980.00"), player.getTotalOnTable());
		assertEquals(new BigDecimal("880.00"), player.getCurrentBalance());
	}
	@Test
	public void resultsAreInCorrectLists() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		multiPlayerTable.bet(usr.getId(), 1, TWENTY);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		var player = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(9, player.getBets().size());
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(1, player.getRoundResults().size());
		assertEquals(4, player.getRoundResults().get(0).playerResult().losingBets().size());
		assertEquals(5, player.getRoundResults().get(0).playerResult().winningBets().size());
		assertEquals(TWENTY, player.getRoundResults().get(0).playerResult().winningBets().get(0).getAmount());
		assertEquals(new BigDecimal("700.00"), player.getRoundResults().get(0).playerResult().winningBets().get(2).getWinAmount());
		assertEquals(5, player.getBets().size());
	}
	@Test
	public void remainingBetsLoseEventually() {
		singlePlayerTable.join(usr);
		var player = singlePlayerTable.getPlayer(usr.userId());
		// Round 1, all bets lose
		singlePlayerTable.bet(usr.getId(), 1, TWENTY);
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("980.00"), player.getTotalOnTable());

		// Round 2 losing bets are FIRST_DOZEN, SECOND_DOZEN, FIRST_HALF, BLACK, ODD, FIRST_COLUMN, SECOND_COLUMN
		// winning bets are THIRD_DOZEN, RED, EVEN, SECOND_HALF, THIRD_COLUMN
		betDefaults(singlePlayerTable);
		assertEquals(new BigDecimal("740.00"), player.getCurrentBalance());
		assertEquals(new BigDecimal("980.00"), player.getTotalOnTable());
		assertEquals(new BigDecimal("240.00"), player.getTotalBet());
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "36");
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("880.00"), player.getCurrentBalance());
		assertEquals(new BigDecimal("980.00"), player.getTotalOnTable());
		RoundResult round2Result = player.getRoundResults().get(1);
		assertEquals(5, round2Result.playerResult().winningBets().size());
		assertEquals(7, round2Result.playerResult().losingBets().size());

		// Round 3 losing bets are THIRD_DOZEN, EVEN, SECOND_HALF, THIRD_COLUMN
		// winning bet is RED
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "1");
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());

		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(new BigDecimal("900.00"), player.getCurrentBalance());
		assertEquals(new BigDecimal("920.00"), player.getTotalOnTable());
		assertEquals(new BigDecimal("20.00"), player.getTotalBet());
		RoundResult round3Result = player.getRoundResults().get(2);
		assertEquals(TWENTY, player.getTotalBet());
		assertEquals(3, round3Result.spinResult().roundNumber());
		assertEquals(1, round3Result.playerResult().winningBets().size());
		assertEquals(4, round3Result.playerResult().losingBets().size());
		assertEquals(new BigDecimal("20.00"), round3Result.playerResult().totalWinnings());
		List<BigDecimal> losingBets = round3Result.playerResult().losingBets().stream().map(RouletteBet::getAmount).toList();
		assertEquals(new BigDecimal("80.00"), Functions.calculateSum(losingBets));
		assertEquals(new BigDecimal("100.00"), round3Result.playerResult().totalBets());
		assertEquals(new BigDecimal("20.00"), round3Result.playerResult().totalRemainingBets());

		// Round 4 last losing bet is RED
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "2");
		singlePlayerTable.play(usr.getId(), singlePlayerTable.getWheel().getSpinId());
		waitForRoundToBeCompleted(singlePlayerTable);
		RoundResult round4Result = player.getRoundResults().get(3);
		assertEquals(4, round4Result.spinResult().roundNumber());
		assertEquals(2, round4Result.spinResult().winningNumber());
		assertEquals(0, round4Result.playerResult().winningBets().size());
		assertEquals(1, round4Result.playerResult().losingBets().size());

		assertEquals(BigDecimal.ZERO, round4Result.playerResult().totalWinnings());
		List<BigDecimal> losingBetsRound4 = round4Result.playerResult().losingBets().stream().map(RouletteBet::getAmount).toList();
		assertEquals(TWENTY, Functions.calculateSum(losingBetsRound4));
		assertEquals(TWENTY, round4Result.playerResult().totalBets());
		assertEquals(BigDecimal.ZERO, round4Result.playerResult().totalRemainingBets());
		assertEquals(new BigDecimal("0.00"), player.getTotalBet());
		assertEquals(new BigDecimal("900.00"), player.getTotalOnTable());
		assertEquals(new BigDecimal("900.00"), player.getCurrentBalance());
	}

	private void betDefaults(RouletteTable_ table) {
		table.bet(usr.getId(), 200, TWENTY); // first dozen
		table.bet(usr.getId(), 201, TWENTY); // second dozen
		table.bet(usr.getId(), 202, TWENTY); // third dozen
		table.bet(usr.getId(), 203, TWENTY); // first half
		table.bet(usr.getId(), 204, TWENTY); // even
		table.bet(usr.getId(), 205, TWENTY); // red
		table.bet(usr.getId(), 206, TWENTY); // black
		table.bet(usr.getId(), 207, TWENTY); // odd
		table.bet(usr.getId(), 208, TWENTY); // second half
		table.bet(usr.getId(), 145, TWENTY); // first column
		table.bet(usr.getId(), 146, TWENTY); // second column
		table.bet(usr.getId(), 147, TWENTY); // third column
	}
}
