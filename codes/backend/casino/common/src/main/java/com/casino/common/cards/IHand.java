package com.casino.common.cards;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IHand extends Comparable<IHand> {

	public List<Integer> calculateValues();

	public void addCard(Card card);

	public List<Card> getCards();

	public boolean isCompleted();

	public boolean isInsured();

	public void insure();

	public boolean shouldComplete();

	public UUID getId();

	public boolean isActive();

	public void activate();

	public void complete();

	public boolean isDoubled();

	public void doubleDown(Card c);

	public boolean isBlackjack();

	public BigDecimal getBet();

	public BigDecimal getInsuranceBet();

	public void updateBet(BigDecimal bet);

	public void stand();

	public Integer calculateFinalValue();

	public boolean hasWinningChance();

	public boolean isInsuranceCompensable();

}
