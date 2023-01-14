package com.casino.common.bank;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.casino.common.cards.IHand;
import com.casino.common.player.ICasinoPlayer;

/**
 * 
 * Bank for cardGames where comparison is made between player's and dealer's cards
 * a.k.a hands
 *
 */
public class CardGameBank {
	private static final Logger LOGGER = Logger.getLogger(CardGameBank.class.getName());
	private static final BigDecimal TWO_AND_HALF = new BigDecimal("2.5");

	public static void matchBalances(List<ICasinoPlayer> players, IHand dealerHand) {
		LOGGER.info("Dealer starts balance matching");
		List<ICasinoPlayer> playersWithWinningChances = players.stream().filter(ICasinoPlayer::hasWinningChance).toList();
		playersWithWinningChances.forEach(player -> payForWinners(player, dealerHand));
	}

	private static void payForWinners(ICasinoPlayer player, IHand dealerHand) {
		player.getHands().forEach(playerHand -> {
			if (shouldPayInsuranceBet(playerHand, dealerHand))
				player.increaseBalanceAndPayout(playerHand.getInsuranceBet().multiply(BigDecimal.TWO));
			BigDecimal betMultiplier = determineBetMultiplier(playerHand, dealerHand);
			BigDecimal winAmount = playerHand.getBet().multiply(betMultiplier);
			player.increaseBalanceAndPayout(winAmount);
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
