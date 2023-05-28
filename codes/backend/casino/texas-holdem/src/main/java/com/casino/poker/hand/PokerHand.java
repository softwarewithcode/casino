package com.casino.poker.hand;

import com.casino.common.cards.Card;
import com.casino.common.cards.CardHand;

public interface PokerHand extends CardHand<PokerHand>, Comparable<PokerHand> {

	Card getMostSignificantCard();

	PokerHandType getType();

	Integer calculateCardsSum();

}
