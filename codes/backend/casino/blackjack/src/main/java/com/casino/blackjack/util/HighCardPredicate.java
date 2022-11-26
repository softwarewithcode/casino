package com.casino.blackjack.util;

import java.util.function.Predicate;

import com.casino.common.cards.Card;

public class HighCardPredicate<E> implements Predicate<Card> {

	@Override
	public boolean test(Card t) {
		return t.getRank() > 10;
	}

}
