package com.casino.common.cards;

public class Card {
	private final int rank;
	private final Suit suit;

	public Card(int rank, Suit suit) {
		super();
		if (rank < 1 || rank > 13)
			throw new IllegalArgumentException("incorrect rank " + rank);
		this.rank = rank;
		this.suit = suit;
	}

	public int getRank() {
		return rank;
	}

	public Suit getSuit() {
		return suit;
	}

	public boolean isAce() {
		return rank == 1;
	}

	public boolean isPictureCard() {
		return rank > 10 && rank < 14;
	}

	@Override
	public String toString() {
		return "Card [rank=" + rank + ", suit=" + suit + "]";
	}

}
