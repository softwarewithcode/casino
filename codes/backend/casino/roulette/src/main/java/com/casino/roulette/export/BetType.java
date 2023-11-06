package com.casino.roulette.export;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BetType {
	SINGLE_NUMBER(35),
	DOUBLE_NUMBER(17),
	TRIPLE_NUMBER(11),
	QUADRUPLE_NUMBER(8),
	SIX_NUMBER(5),
	FIRST_DOZEN(2),
	SECOND_DOZEN(2),
	THIRD_DOZEN(2),
	FIRST_COLUMN(2),
	SECOND_COLUMN(2),
	THIRD_COLUMN(2),
	RED(1),
	BLACK(1),
	EVEN(1),
	ODD(1),
	FIRST_HALF(1),
	SECOND_HALF(1);

	private final Integer paysOut;

	BetType(Integer payout) {
		this.paysOut = payout;
	}

	public Integer getPaysOut() {
		return paysOut;
	}
	
	public String getName() {
		return this.name();
	}
}
