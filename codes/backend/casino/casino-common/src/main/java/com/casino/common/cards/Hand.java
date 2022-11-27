package com.casino.common.cards;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Hand implements IHand {
	private final UUID id;
	private final Instant created;
	private List<Card> cards;

	public Hand(UUID id) {
		this.id = id;
		this.created = Instant.now();
		cards = new ArrayList<Card>();
	}

	@Override
	public int calculateValue() {
		// TODO Auto-generated method stub
		return 0;
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
		Hand other = (Hand) obj;
		return Objects.equals(id, other.id);
	}

}
