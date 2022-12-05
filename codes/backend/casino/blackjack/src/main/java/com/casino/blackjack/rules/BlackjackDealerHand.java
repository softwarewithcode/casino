package com.casino.blackjack.rules;

import java.util.List;
import java.util.UUID;

import com.casino.blackjack.player.BlackjackHand;

public class BlackjackDealerHand extends BlackjackHand {

	public BlackjackDealerHand(UUID id, boolean active) {
		super(id, active);
	}

	@Override
	public boolean shouldCompleteHand() {
		List<Integer> values = calculateValues();
		if (values.size() == 1)
			return values.get(0) >= 17;
		return values.get(1) >= 17;
	}
}
