package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.ISeatedTable;

public class BlackjackPlayer extends CasinoPlayer {
	private List<IHand> hands;

	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, ISeatedTable table) {
		super(name, id, startBalance, table);
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

	@Override
	public List<IHand> getHands() {
		return hands;
	}

	@Override
	public String toString() {
		return "[name=" + getName() + ", id=" + getId() + ", hands=" + hands + "]";
	}

}
