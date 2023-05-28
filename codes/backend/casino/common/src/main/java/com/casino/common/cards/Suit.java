package com.casino.common.cards;

public enum Suit {
	SPADE(4), HEART(3), DIAMOND(2), CLUB(1);

	private final int value;

	 Suit(int semanticValue) {
		this.value = semanticValue;
	}

	public int getValue() {
		return value;
	}
}
