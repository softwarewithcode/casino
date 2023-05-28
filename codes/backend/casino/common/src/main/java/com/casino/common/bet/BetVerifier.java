package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.exception.IllegalBetException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.structure.ICasinoTable;

public class BetVerifier {

	public static void verifyBetIsAllowedInTable(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		verify(table, player, betAttempt);
		verifySufficientBalance(betAttempt, player);
	}

	private static void verify(ICasinoTable table, ICasinoPlayer player, BigDecimal betAttempt) {
		if (betAttempt == null)
			throw new IllegalBetException("no bet is given in table:" + table + " player:" + player, 2);
		if (betAttempt.equals(BigDecimal.ZERO))
			return; // Resets bet
		if (player.getCurrentBalance().compareTo(betAttempt) < 0)
			throw new IllegalBetException("given bet is more than player can afford " + table + " player:" + player + " bet" + betAttempt, 3);
		if (betAttempt.compareTo(table.getDealer().getGameData().getMinBet()) < 0)
			throw new IllegalBetException("given bet is under table's minimum bet " + table.getDealer().getGameData().getMinBet().toString() + " was:" + betAttempt, 4);
		if (betAttempt.compareTo(table.getDealer().getGameData().getMaxBet()) > 0)
			throw new IllegalBetException("given bet is over table's maximum bet " + table.getDealer().getGameData().getMaxBet().toString() + " was:" + betAttempt, 5);
		if (betAttempt.compareTo(BigDecimal.ZERO) < 0)
			throw new IllegalBetException("bet cannot be negative" + table + " player:" + player, 9);
	}

	public static void verifySufficientBalance(BigDecimal requiredBalance, ICasinoPlayer player) {
		if (player.getCurrentBalance().compareTo(requiredBalance) < 0)
			throw new IllegalArgumentException("player has not enough money: Should have " + requiredBalance.toString() + " has: " + player.getCurrentBalance().toString());
	}

}