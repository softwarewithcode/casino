package com.casino.poker.hand;

import com.casino.common.cards.Card;

import java.util.List;
import java.util.UUID;

public final class HoldemHand implements PokerHand {
	private final List<Card> finalCards;
	private  PokerHandType type;

	public HoldemHand(List<Card> cards, PokerHandType name) {
		if (cards.size() != 5)
			throw new IllegalArgumentException("card size does not match " + cards.size());
		this.finalCards = cards;
		this.type = name;
	}

	@Override
	public int compareTo(PokerHand other) {
		return PokerHandComparator.compare(this, other);
	}

	@Override
	public String toString() {
		return "HoldemHand [" + (type != null ? "type=" + type : "") + "]";
	}

	@Override
	public Integer calculateCardsSum() {
		if (this.finalCards.size() != 5)
			throw new RuntimeException("Not enough cards to sum " + this.finalCards.size());
		return this.finalCards.stream().mapToInt(Card::getRank).sum();
	}


	@Override
	public Card getMostSignificantCard() {
		return this.finalCards.get(0);
	}

	@Override
	public List<Card> getCards() {
		return finalCards;
	}

	@Override
	public UUID getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PokerHandType getType() {
		return this.type;
	}

	@Override
	public void addCard(Card card) {
//		this.privateCards.add(card);

	}

	public void setType(PokerHandType type) {
		this.type = type;
	}


}
