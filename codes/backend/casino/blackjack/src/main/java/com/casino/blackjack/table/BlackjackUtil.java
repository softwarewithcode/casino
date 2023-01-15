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
}
