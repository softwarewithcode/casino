package com.casino.blackjack.dealer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.blackjack.player.BlackjackHand;
import com.casino.common.cards.IHand;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@JsonIgnore
	@Override
	public BigDecimal getBet() {
		// TODO Auto-generated method stub
		return super.getBet();
	}

	@JsonIgnore
	@Override
	public boolean isDoubled() {
		// TODO Auto-generated method stub
		return super.isDoubled();
	}

	@JsonIgnore
	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return super.isCompleted();
	}

	@JsonIgnore
	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return super.isActive();
	}

	@JsonIgnore
	@Override
	public boolean isInsured() {
		// TODO Auto-generated method stub
		return super.isInsured();
	}

	@JsonIgnore
	@Override
	public BigDecimal getInsuranceBet() {
		// TODO Auto-generated method stub
		return super.getInsuranceBet();
	}

	@JsonIgnore
	@Override
	public boolean isInsuranceCompensable() {
		// TODO Auto-generated method stub
		return super.isInsuranceCompensable();
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
