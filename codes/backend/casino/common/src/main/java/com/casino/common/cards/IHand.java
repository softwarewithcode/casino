package com.casino.common.cards;

import java.util.List;
import java.util.UUID;

public interface IHand {

	public List<Integer> calculateValues();

	public void addCard(Card card);

	public List<Card> getCards();

	public boolean isCompleted();

	public UUID getId();

	public boolean isActive();

	public void activate();

	public void complete();

	public boolean isDoubled();

	public void doubleDown();

	public boolean isBlackjack();
}
