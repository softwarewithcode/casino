package com.casino.common.bank;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.casino.common.cards.IHand;
import com.casino.common.player.ICasinoPlayer;

public class Bank {
	private static final Logger LOGGER = Logger.getLogger(Bank.class.getName());
	private static final BigDecimal BLACKJACK_FACTOR = new BigDecimal("2.5");

	public static void matchBalances(List<ICasinoPlayer> allPlayers, IHand dealerHand) {
		LOGGER.info("Dealer starts balance matching");
		List<ICasinoPlayer> playersWithWinningChances = allPlayers.stream().filter(ICasinoPlayer::hasWinningChance).toList();
		playersWithWinningChances.forEach(player -> checkWinnersAndPay(player, dealerHand));
	}

	private static void checkWinnersAndPay(ICasinoPlayer player, IHand dealerHand) {
		player.getHands().forEach(playerHand -> {
			if (shouldPayInsuranceBet(playerHand, dealerHand))
				player.increaseBalanceAndPayout(playerHand.getInsuranceBet().multiply(BigDecimal.TWO));
			BigDecimal betMultiplier = determineBetMultiplier(playerHand, dealerHand);
			BigDecimal handPayout = playerHand.getBet().multiply(betMultiplier);
			player.increaseBalanceAndPayout(handPayout);
		});
	}

	private static boolean shouldPayInsuranceBet(IHand playerHand, IHand dealerHand) {
		return playerHand.isInsuranceCompensable() && dealerHand.isBlackjack();
	}

	private static BigDecimal determineBetMultiplier(IHand playerHand, IHand dealerHand) {
		int handComparison = dealerHand.compareTo(playerHand);
		if (evenResult(handComparison))
			return BigDecimal.ONE;
		if (playerWins(handComparison))
			return playerHand.isBlackjack() ? BLACKJACK_FACTOR : BigDecimal.TWO;
		return BigDecimal.ZERO;
	}

	private static boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private static boolean playerWins(int comparison) {
		return comparison > 0;
	}
}
