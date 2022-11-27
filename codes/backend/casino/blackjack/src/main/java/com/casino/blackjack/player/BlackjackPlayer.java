package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.player.CasinoPlayer;

public class BlackjackPlayer extends CasinoPlayer {
	private List<IHand> hands;
	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, BigDecimal endBalance) {
		super(name, id, startBalance, endBalance);
		hands = new ArrayList<IHand>();
		hands.add(createNewHand());
	}

	private BlackjackHand createNewHand() {
		return new BlackjackHand(UUID.randomUUID());
	}

	public void addCard(IHand hand, Card card) {
		if (hand == null)
			throw new IllegalArgumentException("cannot add card to non existing hand");
		hand.addCard(card);
	}

	public void clearHands() {
		hands.clear();
	}

	public List<IHand> getHands() {
		return hands;
	}
}
