package com.casino.blackjack.table;

import java.util.logging.Logger;

import com.casino.common.cards.Card;

public class BlackjackUtil {
	private static final Logger LOGGER = Logger.getLogger(BlackjackUtil.class.getName());

	public static boolean haveSameValue(Card card, Card card2) {
		if (card.getRank() == card2.getRank())
			return true;
		return card.getRank() >= 10 && card.getRank() <= 13 && card2.getRank() >= 10 && card2.getRank() <= 13;
	}

	public static void dumpTable(BlackjackTable table, String info) {
		LOGGER.info("Table error:" + info + " =" + table); // Plus relevant field which toString does not cover
	}

//	public static void validateDoubleDownPreConditions(IHand hand) {
//		if (hand.isDoubled())
//			throw new IllegalPlayerActionException("hand has been doubled before ", 10);
//		if (hand.isBlackjack())
//			throw new IllegalPlayerActionException("blackjack cannot be doubled ", 10);
//		if (!hand.isActive())
//			throw new IllegalPlayerActionException("hand is not active cannot be doubled ", 10);
//		List<Integer> values = hand.calculateValues();
//		int val = values.get(0);
//		if (!(val >= 9 && val <= 11))
//			throw new IllegalPlayerActionException("hand value does not allow doubling; " + hand.getCards().get(0) + " " + hand.getCards().get(1), 10);
//		if (hand.getCards().size() != 2)
//			throw new IllegalPlayerActionException("starting hand does not contain exactly two cards:" + hand, 3);
//	}

}
