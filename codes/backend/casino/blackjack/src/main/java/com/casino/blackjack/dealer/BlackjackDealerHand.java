package com.casino.blackjack.dealer;

import java.util.List;
import java.util.UUID;

import com.casino.blackjack.player.BlackjackHand;
import com.casino.common.cards.IHand;

public class BlackjackDealerHand extends BlackjackHand implements Comparable<IHand> {

	public BlackjackDealerHand(UUID id, boolean active) {
		super(id, active);
	}

	@Override
	public boolean shouldComplete() {
		List<Integer> values = calculateValues();
		if (values.size() == 1)
			return values.get(0) >= 17;
		return values.get(1) >= 17;
	}

	@Override
	public int compareTo(IHand playerHand) {
		if (playerHand == null)
			return -1;
		int dealerVal = calculateFinalValue();
		int playerVal = playerHand.calculateFinalValue();
		boolean dealerBlackjack = this.isBlackjack();
		boolean playerBlackjack = playerHand.isBlackjack();
		if (dealerBlackjack && playerBlackjack)
			return 0;
		if (dealerBlackjack && !playerBlackjack)
			return -1;
		if (!dealerBlackjack && playerBlackjack)
			return 1;
		if (dealerVal > 21 && playerVal > 21)
			return -1;
		if (dealerVal > 21 && playerVal <= 21)
			return 1;
		if (dealerVal <= 21 && playerVal > 21)
			return -1;
		if (dealerVal == playerVal)
			return 0;
		return dealerVal > playerVal ? -1 : 1;
	}
}
