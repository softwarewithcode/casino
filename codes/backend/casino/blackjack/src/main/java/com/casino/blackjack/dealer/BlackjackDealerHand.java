package com.casino.blackjack.dealer;

import java.util.List;
import java.util.UUID;

import com.casino.blackjack.player.BlackjackHand;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = { "cards", "values", "blackjack" })
public class BlackjackDealerHand extends BlackjackHand {

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

}
