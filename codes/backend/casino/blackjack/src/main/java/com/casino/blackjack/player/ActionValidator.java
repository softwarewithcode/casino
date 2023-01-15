package com.casino.blackjack.player;

import java.util.List;

import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.IllegalPlayerActionException;

public class ActionValidator {
	public static void validateInsuringConditions(BlackjackPlayer player) {
		validateActionConditions(player);
		if (player.getFirstHand().isInsured())
			throw new IllegalPlayerActionException("hand has been insured earlier ");
		if (player.getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("cannot insure, hand has been doubled earlier ");
		if (player.getFirstHand().isBlackjack())
			throw new IllegalPlayerActionException("cannot insure, hand is blackjack ");
	}

	public static void validateDoubleDownPreConditions(BlackjackPlayer player) {
		validateActionConditions(player);
		if (player.getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("hand has been doubled before ");
		if (player.getFirstHand().isBlackjack())
			throw new IllegalPlayerActionException("blackjack cannot be doubled ");
		List<Integer> values = player.getFirstHand().calculateValues();
		int val = values.get(0);
		if (!(val >= 9 && val <= 11))
			throw new IllegalPlayerActionException("hand value does not allow doubling; " + player.getFirstHand().getCards().get(0) + " " + player.getFirstHand().getCards().get(1));
	}

	public static void validateSplitPreConditions(BlackjackPlayer player) {
		validateActionConditions(player);
		if (!BlackjackUtil.haveSameValue(player.getFirstHand().getCards().get(0), player.getFirstHand().getCards().get(1)))
			throw new IllegalPlayerActionException("not equal values");
		if (player.getFirstHand().isInsured())
			throw new IllegalPlayerActionException("cannot split insured hand");
		if (player.getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("hand has been doubled before ");
	}

	public static boolean isSplitAllowed(BlackjackPlayer player) {
		try {
			validateSplitPreConditions(player);
			return true;
		} catch (IllegalPlayerActionException i) {
			return false;
		}
	}

	public static boolean isDoubleDownAllowed(BlackjackPlayer player) {
		try {
			validateDoubleDownPreConditions(player);
			return true;
		} catch (IllegalPlayerActionException i) {
			return false;
		}
	}

	private static void validateActionConditions(BlackjackPlayer player) {
		if (player.getHands().size() != 1)
			throw new IllegalPlayerActionException("wrong hand count:" + player.getUserName() + " " + player.getHands().size());
		if (!player.getHands().get(0).isActive())
			throw new IllegalPlayerActionException("first hand is not active " + player.getUserName() + " " + player.getFirstHand());
		if (player.getHands().get(0).getCards().size() != 2)
			throw new IllegalPlayerActionException("starting hand does not contain exactly two cards:" + player.getUserName() + " " + player.getHands().get(0).getCards());
		try {
			BetVerifier.verifySufficentBalance(player.getFirstHand().getBet(), player);
		} catch (IllegalArgumentException i) {
			throw new IllegalPlayerActionException(i.getMessage());
		}
	}
}
