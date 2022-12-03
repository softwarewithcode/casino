package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.exception.IllegalBetException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;
import com.casino.common.table.phase.GamePhase;

public class BetUtil {

	public static void verifyBet(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		verify(table, player, betAttempt);
		verifySufficentBalance(betAttempt, player);
	}

	private static void verify(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		if (table.getGamePhase() != GamePhase.BET)
			throw new IllegalBetException("given bet in wrong phase:" + table + " player:" + player + " bet:" + betAttempt.toString(), 1);
		if (betAttempt == null)
			throw new IllegalBetException("no bet is given in table:" + table + " player:" + player, 2);
		if (player.getBalance().compareTo(betAttempt) < 0)
			throw new IllegalBetException("given bet is more than player can afford " + table + " player:" + player + " bet" + betAttempt.toString(), 3);
		if (betAttempt.compareTo(table.getBetValues().minimumBet()) < 0)
			throw new IllegalBetException("given bet is under table minimum bet " + table.getBetValues().minimumBet().toString() + " bet:" + betAttempt.toString(), 4);
		if (betAttempt.compareTo(table.getBetValues().maximumBet()) > 0)
			throw new IllegalBetException("given bet is over table maximum bet " + table.getBetValues().maximumBet().toString() + " bet:" + betAttempt.toString(), 5);
	}

	public static void verifySufficentBalance(BigDecimal attempt, ICasinoPlayer player) {
		if (player.getBalance().compareTo(attempt) < 0)
			throw new IllegalArgumentException("player has not enough money: Should have " + attempt.toString() + " has: " + player.getBalance().toString());

	}
}