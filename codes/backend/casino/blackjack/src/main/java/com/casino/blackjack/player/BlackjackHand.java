package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPlayerActionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Player(s) and internal timers are possible concurrent actors. BlackjackPlayer getHands() - hands out references to these hands.
 * 
 */
@JsonIncludeProperties(value = { "cards", "values", "blackjack", "bet", "insured", "active" })
public class BlackjackHand implements IHand {
	private final UUID id;
	private final Instant created;
	private final List<Card> cards;
	private volatile Instant completed;
	private volatile boolean active;
	private volatile boolean doubled;
	private volatile BigDecimal bet;
	private volatile BigDecimal insuranceBet;
	private ReentrantLock lock;

	public BlackjackHand(UUID id, boolean active) {
		this.id = id;
		this.created = Instant.now();
		cards = new ArrayList<Card>();
		this.active = active;
		lock = new ReentrantLock();
	}

	@Override
	public List<Integer> calculateValues() {
		List<Integer> values = new ArrayList<Integer>(2);
		Integer smallestValue = cards.stream().collect(Collectors.summingInt(card -> card.getRank() > 10 ? 10 : card.getRank()));
		values.add(smallestValue);
		Optional<Card> aceOptional = cards.stream().filter(card -> card.getRank() == 1).findAny();
		if (hasTwoPossibleValues(smallestValue, aceOptional)) {
			values.add(Integer.valueOf(smallestValue + 10));
		}
		return values;
	}

	public boolean isBlackjack() {
		List<Integer> vals = calculateValues();
		return cards.size() == 2 && vals.size() == 2 && vals.get(1) == 21;
	}

	@JsonProperty
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

	public Instant getCreated() {
		return created;
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

	@Override
	public boolean isCompleted() {
		return completed != null;
	}

	@Override
	public void complete() {
		try {
//			lock.lock();
			if (isCompleted())
				throw new IllegalStateException("complete called on completed hand");
			this.completed = Instant.now();
			this.active = false;
		} finally {
//			lock.unlock();
		}
	}

	@Override
	public boolean isActive() {
		return active && !isCompleted();
	}

	@Override
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

	@Override
	public void doubleDown(Card ref) {
		try {
			lock.lock();
			if (!isActive() || isDoubled())
				throw new IllegalPlayerActionException("doubling not allowed active:" + this, 15);
			this.doubled = true;
			this.bet = this.bet.multiply(BigDecimal.TWO);
			this.cards.add(ref);
			this.complete();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void stand() {
		try {
			lock.lock();
			if (!isActive())
				throw new IllegalPlayerActionException("stand called on inactive hand", 15);
			this.complete();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean shouldComplete() {
		if (isCompleted())
			return false;
		if (isDoubled())
			return true;
		List<Integer> vals = calculateValues();
		if (vals.size() == 2 && vals.get(1) == 21)
			return true;
		return vals.get(0) >= 21 ? true : false;
	}

	@Override
	public Integer calculateFinalValue() {
		if (!isCompleted() || getCards().size() < 2)
			throw new IllegalArgumentException("hand has not enough cards " + getCards().size() + " or is not completed:" + completed);
		List<Integer> values = calculateValues();
		Integer first = values.get(0);
		if (values.size() != 2)
			return first;
		return values.get(1) > 21 ? first : values.get(1);
	}

	@Override
	public boolean isInsured() {
		return insuranceBet != null;
	}

	@Override
	public void insure() {
		try {
			lock.lock();
			if (!isActive() || isInsured() || isBlackjack())
				throw new IllegalPlayerActionException("insurance not allowed:" + this, 15);
			insuranceBet = bet.divide(BigDecimal.TWO);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean hasWinningChance() {
		return calculateFinalValue() <= 21 || isInsured();
	}

	public BigDecimal getInsuranceBet() {
		return insuranceBet;
	}

	@JsonIgnore
	@Override
	public boolean isInsuranceCompensable() {
		return isInsured() && isCompleted();
	}

}
