package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.exception.IllegalBetException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;
import com.casino.common.table.phase.GamePhase;

public class BetUtil {

	public static void verifyStartingBet(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		verify(table, player, betAttempt);
		verifySufficentBalance(betAttempt, player);
	}

	private static void verify(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		if (table.getGamePhase() != GamePhase.BET)
			throw new IllegalBetException("given bet in wrong phase: " + table.getGamePhase() + " table:" + table + " player:" + player + " bet:" + betAttempt.toString(), 1);
		if (betAttempt == null)
			throw new IllegalBetException("no bet is given in table:" + table + " player:" + player, 2);
		if (betAttempt.equals(BigDecimal.ZERO))
			return; // Resets bet
		if (player.getBalance().compareTo(betAttempt) < 0)
			throw new IllegalBetException("given bet is more than player can afford " + table + " player:" + player + " bet" + betAttempt.toString(), 3);
		if (betAttempt.compareTo(table.getTableCard().getThresholds().minimumBet()) < 0)
			throw new IllegalBetException("given bet is under table's minimum bet " + table.getTableCard().getThresholds().minimumBet().toString() + " was:" + betAttempt.toString(), 4);
		if (betAttempt.compareTo(table.getTableCard().getThresholds().maximumBet()) > 0)
			throw new IllegalBetException("given bet is over table's maximum bet " + table.getTableCard().getThresholds().maximumBet().toString() + " was:" + betAttempt.toString(), 5);
		if (betAttempt.compareTo(BigDecimal.ZERO) < 0)
			throw new IllegalBetException("bet cannot be negative" + table + " player:" + player, 9);
	}

	public static void verifySufficentBalance(BigDecimal attempt, ICasinoPlayer player) {
		if (player.getBalance().compareTo(attempt) < 0)
			throw new IllegalArgumentException("player has not enough money: Should have " + attempt.toString() + " has: " + player.getBalance().toString());
	}

}