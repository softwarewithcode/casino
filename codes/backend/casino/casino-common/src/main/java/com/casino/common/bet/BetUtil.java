package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.common.IllegalBetException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;
import com.casino.common.table.Phase;

public class BetUtil {

	public static void verifyBet(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		verify(table, player, betAttempt);
		verifySufficentAmount(betAttempt, player);
	}

	private static void verify(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		if (table.getPhase() != Phase.BET)
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

	public static void verifySufficentAmount(BigDecimal amount, ICasinoPlayer player) {
		if (player.getInitialBalance().compareTo(amount) < 0)
			throw new IllegalArgumentException("player has not enough money: Should have " + amount.toString() + " has: " + player.getInitialBalance().toString());

	}
}