package com.casino.blackjack.player;

import com.casino.common.cards.Card;
import com.casino.common.cards.CardHand;
import com.casino.common.exception.IllegalPlayerActionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@JsonIncludeProperties(value = { "cards", "values", "blackjack", "bet", "insured", "doubled", "active" })
public class BlackjackHand implements CardHand<BlackjackHand> {
	private final UUID id;
	private final Instant created;
	private final List<Card> cards;
	private volatile Instant completed;
	private volatile boolean active;
	private volatile boolean doubled;
	private volatile BigDecimal bet;
	private volatile BigDecimal insuranceBet;
	private final ReentrantLock lock;

	public BlackjackHand(UUID id, boolean active) {
		this.id = id;
		this.created = Instant.now();
		cards = new ArrayList<>();
		this.active = active;
		lock = new ReentrantLock();
	}

	public List<Integer> calculateValues() {
		List<Integer> values = new ArrayList<>(2);
		Integer smallestValue = cards.stream().mapToInt(card -> Math.min(card.getRank(), 10)).sum();
		values.add(smallestValue);
		Optional<Card> aceOptional = cards.stream().filter(card -> card.getRank() == 1).findAny();
		if (hasTwoPossibleValues(smallestValue, aceOptional))
			values.add(smallestValue + 10);
		return values;
	}

	public boolean isBlackjack() {
		List<Integer> vals = calculateValues();
		return cards.size() == 2 && vals.size() == 2 && vals.get(1) == 21;
	}
	@JsonProperty // getter for serialization
	public List<Integer> getValues() {
		if (!isCompleted())
			return calculateValues();
		List<Integer> values = new ArrayList<>();
		values.add(calculateFinalValue());
		return values;
	}

	private boolean hasTwoPossibleValues(Integer smallestValue, Optional<Card> aceOptional) {
		return aceOptional.isPresent() && smallestValue + 10 < 22;
	}

	public void updateBet(BigDecimal bet) {
		this.bet = bet;
	}

	public BigDecimal getBet() {
		return bet;
	}

	@Override
	public void addCard(Card card) {
		try {
			lock.lock();
			if (card == null)
				throw new IllegalArgumentException("Card is missing");
			if (isCompleted()) // inactive hand can still get a card in split.
				throw new IllegalStateException("Hand is completed, cannot add card " + this);
			this.cards.add(card);
			if (shouldComplete())
				complete();
		} finally {
			lock.unlock();
		}
	}

	public boolean isDoubled() {
		return doubled;
	}

	@Override
	public List<Card> getCards() {
		return cards;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "BlackjackHand [id=" + id + ", created=" + created + ", cards=" + cards + ", completed=" + completed + ", active=" + active + ", doubled=" + doubled + ", bet=" + bet + ", insuranceBet=" + insuranceBet + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlackjackHand other = (BlackjackHand) obj;
		return Objects.equals(id, other.id);
	}

	public boolean isCompleted() {
		return completed != null;
	}

	public void complete() {
		try {
			lock.lock();
			if (isCompleted())
				throw new IllegalStateException("complete called on completed hand");
			this.completed = Instant.now();
			this.active = false;
		} finally {
			lock.unlock();
		}
	}

	public boolean isActive() {
		return active && !isCompleted();
	}

	public void activate() {
		try {
			lock.lock();
			if (isCompleted())
				throw new IllegalStateException("cannot activate completed hand " + this);
			active = true;
		} finally {
			lock.unlock();
		}
	}

	public void doubleDown(Card ref) {
		try {
			lock.lock();
			if (!isActive() || isDoubled())
				throw new IllegalPlayerActionException("doubling not allowed active:" + this);
			this.doubled = true;
			this.bet = this.bet.multiply(new BigDecimal("2"));
			this.cards.add(ref);
			this.complete();
		} finally {
			lock.unlock();
		}
	}

	public void stand() {
		try {
			lock.lock();
			if (!isActive())
				throw new IllegalPlayerActionException("stand called on inactive hand");
			this.complete();
		} finally {
			lock.unlock();
		}
	}

	public boolean shouldComplete() {
		if (isCompleted())
			return false;
		if (isDoubled())
			return true;
		List<Integer> vals = calculateValues();
		if (vals.size() == 2 && vals.get(1) == 21)
			return true;
		return vals.get(0) >= 21;
	}

	public Integer calculateFinalValue() {
		if (!isCompleted() || getCards().size() < 2)
			throw new IllegalArgumentException("hand has not enough cards " + getCards().size() + " or is not completed:" + completed);
		List<Integer> values = calculateValues();
		Integer first = values.get(0);
		if (values.size() != 2)
			return first;
		return values.get(1) > 21 ? first : values.get(1);
	}

	public boolean isInsured() {
		return insuranceBet != null;
	}

	public void insure() {
		try {
			lock.lock();
			if (!isActive() || isInsured() || isBlackjack())
				throw new IllegalPlayerActionException("insurance not allowed:" + this);
			insuranceBet = bet.divide(BigDecimal.TWO,RoundingMode.DOWN);
		} finally {
			lock.unlock();
		}
	}

	public boolean hasWinningChance() {
		return containsCards() && bet != null && isCompleted() && calculateFinalValue() <= 21 || isInsured();
	}

	private boolean containsCards() {
		return cards.size() > 0;
	}

	public BigDecimal getInsuranceBet() {
		return insuranceBet;
	}

	@JsonIgnore
	public boolean isInsuranceCompensable() {
		return isInsured() && isCompleted();
	}

	public int compareTo(BlackjackHand dealerHand) {
		if (dealerHand == null)
			return -1;
		int dealerVal = calculateFinalValue();
		int playerVal = dealerHand.calculateFinalValue();
		boolean dealerBlackjack = this.isBlackjack();
		boolean playerBlackjack = dealerHand.isBlackjack();
		if (dealerBlackjack && playerBlackjack)
			return 0;
		if (dealerBlackjack)
			return -1;
		if (playerBlackjack)
			return 1;
		if (dealerVal > 21 && playerVal > 21)
			return -1;
		if (dealerVal > 21)
			return 1;
		if (playerVal > 21)
			return -1;
		if (dealerVal == playerVal)
			return 0;
		return dealerVal > playerVal ? -1 : 1;
	}

}
