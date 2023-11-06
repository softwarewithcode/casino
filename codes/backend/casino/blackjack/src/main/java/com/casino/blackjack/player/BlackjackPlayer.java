package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.casino.common.cards.Card;
import com.casino.common.player.CardPlayer;

public interface BlackjackPlayer extends CardPlayer {
	void stand();

	void activateSecondHand();

	void doubleDown(Card card);
	void splitStartingHand();

	void updateStartingBet(BigDecimal bet);

	void insure();

	void hit(Card card);

	List<BlackjackHand> getHands();

	BlackjackHand getFirstHand();

	boolean hasDoubled();

	boolean hasInsured();

	boolean hasCompletedFirstHand();

	Integer getFirstHandFinalValue();

	BigDecimal getBet(int handNumber);

	<T> Optional<T> autoplay(T t);

	void updateAvailableActions();

	boolean canTake();

	BlackjackHand getActiveHand();
}
