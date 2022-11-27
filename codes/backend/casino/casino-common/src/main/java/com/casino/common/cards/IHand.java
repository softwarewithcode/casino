package com.casino.common.cards;

import java.util.List;
import java.util.UUID;

public interface IHand {

	public int calculateValue();

	public void addCard(Card card);

	public List<Card> getCards();

	public UUID getId();
}
