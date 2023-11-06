package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.casino.blackjack.util.BlackjackUtil;
import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.IllegalPlayerActionException;

//Checks if action is technically allowed. Caller needs to check GamePhase
public class BlackjackActionValidator {

	public static void validateInsureAction(BlackjackPlayer_ player) {
		validateBasicConditions(player);
		if (player.getFirstHand().isInsured())
			throw new IllegalPlayerActionException("hand already insured");
		BigDecimal insuranceAmount = player.getFirstHand().getBet().divide(BigDecimal.TWO,RoundingMode.DOWN);
		validateBalance(insuranceAmount, player);
	}

	public static void validateDoubleDownAction(BlackjackPlayer_ player) {
		validateBasicConditions(player);
		List<Integer> values = player.getFirstHand().calculateValues();
		int val = values.get(0);
		if (!(val >= 9 && val <= 11))
			throw new IllegalPlayerActionException("hand value does not allow doubling:" + player.getFirstHand().getCards().get(0) + " " + player.getFirstHand().getCards().get(1));
		validateBalance(player.getFirstHand().getBet(), player);
	}

	private static void validateBalance(BigDecimal requiredBalance, BlackjackPlayer_ player) {
		try {
			BetVerifier.verifySufficientBalance(requiredBalance, player);
		} catch (Exception e) {
			throw new IllegalPlayerActionException(e.getMessage());
		}
	}

	public static void validateSplitAction(BlackjackPlayer_ player) {
		validateBasicConditions(player);
		if (!BlackjackUtil.haveSameValue(player.getFirstHand().getCards().get(0), player.getFirstHand().getCards().get(1)))
			throw new IllegalPlayerActionException("not equal values");
		if (player.getFirstHand().isInsured())
			throw new IllegalPlayerActionException("cannot split insured hand");
		validateBalance(player.getFirstHand().getBet(), player);
	}

	public static boolean isSplitTechnicallyAllowed(BlackjackPlayer_ player) {
		try {
			validateSplitAction(player);
			return true;
		} catch (RuntimeException re) {
			return false;
		}
	}

	public static boolean isDoubleDownTechnicallyAllowed(BlackjackPlayer_ player) {
		try {
			validateDoubleDownAction(player);
			return true;
		} catch (RuntimeException re) {
			return false;
		}
	}

	private static void validateBasicConditions(BlackjackPlayer_ player) {
		if (player.getHands().size() != 1)
			throw new IllegalPlayerActionException("wrong hand count:" + player.getUserName() + " " + player.getHands().size());
		if (!player.getFirstHand().isActive())
			throw new IllegalPlayerActionException("first hand is not active" + player.getUserName() + " " + player.getFirstHand());
		if (player.getFirstHand().getCards().size() != 2)
			throw new IllegalPlayerActionException("hand doesn't have exactly two cards:" + player.getUserName() + " " + player.getFirstHand().getCards());
		if (player.getFirstHand().isBlackjack())
			throw new IllegalPlayerActionException("hand is blackjack");
		if (player.getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("hand already doubled");
	}
}
