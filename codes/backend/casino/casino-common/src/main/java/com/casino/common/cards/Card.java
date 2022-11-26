package com.casino.common.cards;

import java.util.Objects;

public class Card {
	private final int rank;
	private final Suit suit;

	public Card(int rank, Suit suit) {
		super();
		this.rank = rank;
		this.suit = suit;
	}

	public int getRank() {
		return rank;
	}

	public Suit getSuit() {
		return suit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(rank, suit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		return rank == other.rank && suit == other.suit;
	}

}
