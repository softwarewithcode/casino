package com.casino.poker.hand;

public enum PokerHandType {

	STRAIGHT_FLUSH(9),
	FOUR_OF_KIND(8),
	FULL_HOUSE(7),
	FLUSH(6),
	STRAIGHT(5),
	THREE_OF_KIND(4),
	TWO_PAIRS(3),
	PAIR(2),
	HIGH_CARD(1);

	private final int value;

	PokerHandType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

}
