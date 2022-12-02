package com.casino.blackjack.table;

import com.casino.common.cards.Card;

public class BlackjackUtil {

	public static boolean haveSameValue(Card card, Card card2) {
		if (card.getRank() == card2.getRank())
			return true;
		return card.getRank() >= 10 && card.getRank() <= 13 && card2.getRank() >= 10 && card2.getRank() <= 13;
	}
}
