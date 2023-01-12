package com.casino.blackjack.dealer;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.casino.common.cards.IHand;
import com.casino.common.player.ICasinoPlayer;

public class PayoutCalculator {
	private static final Logger LOGGER = Logger.getLogger(PayoutCalculator.class.getName());
	private static final BigDecimal BLACKJACK_FACTOR = new BigDecimal("2.5");
	private BlackjackDealerHand dealerHand;
	private List<ICasinoPlayer> playersWithBet;

	public PayoutCalculator(BlackjackDealerHand dealerHand, List<ICasinoPlayer> playersWithBet) {
		super();
		this.dealerHand = dealerHand;
		this.playersWithBet = playersWithBet;
	}

	@Override
	public String toString() {
		return "Payments [dealerHand=" + dealerHand + ", playersWithBet=" + playersWithBet + "]";
	}

	public void calculate() {
		LOGGER.info("Dealer starts payout");
		List<ICasinoPlayer> playersWithWinningChances = playersWithBet.stream().filter(ICasinoPlayer::hasWinningChance).toList();
		playersWithWinningChances.forEach(player -> payoutWinnings(player));
	}

	private void payoutWinnings(ICasinoPlayer player) {
		player.getHands().forEach(playerHand -> {
			if (shouldPayInsuranceBet(playerHand))
				player.increaseBalanceAndPayout(playerHand.getInsuranceBet().multiply(BigDecimal.TWO));
			BigDecimal betMultiplier = determineBetMultiplier(playerHand);
			BigDecimal handPayout = playerHand.getBet().multiply(betMultiplier);
			player.increaseBalanceAndPayout(handPayout);
		});
	}

	private boolean shouldPayInsuranceBet(IHand playerHand) {
		return playerHand.isInsuranceCompensable() && dealerHand.isBlackjack();
	}

	private BigDecimal determineBetMultiplier(IHand playerHand) {
		int handComparison = dealerHand.compareTo(playerHand);
		if (evenResult(handComparison))
			return BigDecimal.ONE;
		if (playerWins(handComparison))
			return playerHand.isBlackjack() ? BLACKJACK_FACTOR : BigDecimal.TWO;
		return BigDecimal.ZERO;
	}

	private boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private boolean playerWins(int comparison) {
		return comparison > 0;
	}
}
