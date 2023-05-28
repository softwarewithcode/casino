package com.casino.common.cards;

public class Card {
	private final int rank;
	private final Suit suit;

	private Card(int rank, Suit suit) {
		super();
		if (rank < 1 || rank > 14)
			throw new IllegalArgumentException("incorrect rank " + rank);
		if (suit == null)
			throw new IllegalArgumentException("suit is missing");
		this.rank = rank;
		this.suit = suit;
	}

	public static Card of(int rank, Suit suit) {
		return new Card(rank, suit);
	}

	public int getRank() {
		return rank;
	}

	public Suit getSuit() {
		return suit;
	}

	public boolean isAce() {
		return rank == 1 || rank == 14;
	}

	@Override
	public String toString() {
		return "Card " + suit + " " + rank;
	}

}
