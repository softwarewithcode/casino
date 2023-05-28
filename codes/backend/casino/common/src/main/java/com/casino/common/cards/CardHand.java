package com.casino.common.cards;

import java.util.List;
import java.util.UUID;

public interface CardHand<T> extends Comparable<T> {
	void addCard(Card card);

	List<Card> getCards();

	UUID getId();

}
