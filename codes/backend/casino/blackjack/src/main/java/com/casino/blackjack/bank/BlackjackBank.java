package com.casino.blackjack.bank;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer_;

/**
 * 
 * Bank for cardGames where comparison is made between player's and dealer's
 * cards a.k.a hands
 *
 */
public class BlackjackBank {
	private static final Logger LOGGER = Logger.getLogger(BlackjackBank.class.getName());
	private static final BigDecimal TWO_AND_HALF = new BigDecimal("2.5");

	public static void matchBalances(List<BlackjackPlayer_> players, BlackjackHand dealerHand) {
		LOGGER.fine("Dealer starts balance matching");
		List<BlackjackPlayer_> playersWithWinningChances = players.stream().filter(BlackjackPlayer_::hasWinningChance).toList();
		playersWithWinningChances.forEach(player -> payForWinners(player, dealerHand));
	}

	private static void payForWinners(BlackjackPlayer_ player, BlackjackHand dealerHand) {
		player.getHands().forEach(playerHand -> {
			if (shouldPayInsuranceBet(playerHand, dealerHand))
				player.increaseBalanceAndPayout(playerHand.getInsuranceBet().multiply(BigDecimal.TWO));
			BigDecimal betMultiplier = determineBetMultiplier(playerHand, dealerHand);
			BigDecimal winAmount = playerHand.getBet().multiply(betMultiplier);
			player.increaseBalanceAndPayout(winAmount);
		});
	}

	private static boolean shouldPayInsuranceBet(BlackjackHand playerHand, BlackjackHand dealerHand) {
		return playerHand.isInsuranceCompensable() && dealerHand.isBlackjack();
	}

	private static BigDecimal determineBetMultiplier(BlackjackHand playerHand, BlackjackHand dealerHand) {
		int handComparison = dealerHand.compareTo(playerHand);
		if (evenResult(handComparison))
			return BigDecimal.ONE;
		if (playerWins(handComparison))
			return playerHand.isBlackjack() ? TWO_AND_HALF : BigDecimal.TWO;
		return BigDecimal.ZERO;
	}

	private static boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private static boolean playerWins(int comparison) {
		return comparison > 0;
	}
}
