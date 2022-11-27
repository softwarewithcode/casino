package com.casino.blackjack.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;

public class BlackjackHand implements IHand {
	private final UUID id;
	private final Instant created;
	private List<Card> cards;

	public BlackjackHand(UUID id) {
		this.id = id;
		this.created = Instant.now();
		cards = new ArrayList<Card>();
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

	private boolean hasTwoPossibleValues(Integer smallestValue, Optional<Card> aceOptional) {
		return aceOptional.isPresent() && smallestValue + 10 < 22;
	}

	@Override
	public void addCard(Card card) {
		if (card == null)
			throw new IllegalArgumentException("Card is missing");
		this.cards.add(card);
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

}
