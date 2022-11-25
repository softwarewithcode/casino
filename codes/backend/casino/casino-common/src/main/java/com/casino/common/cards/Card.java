package com.casino.common.cards;

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

}
