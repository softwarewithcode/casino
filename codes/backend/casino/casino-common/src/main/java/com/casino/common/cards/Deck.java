package com.casino.common.cards;

import java.util.HashSet;
import java.util.Set;

public class Deck implements IDeck {
	public Set<Card> cards;

	public Deck() {
		create();
	}

	private void create() {
		cards = new HashSet<Card>();
		for (int i = 1; i < 14; i++) {
			Card spade = new Card(i, Suit.SPADE);
			cards.add(spade);
			Card diamond = new Card(i, Suit.DIAMOND);
			cards.add(diamond);
			Card club = new Card(i, Suit.CLUB);
			cards.add(club);
			Card heart = new Card(i, Suit.HEART);
			cards.add(heart);
		}
	}

	public void shuffle() {
		// TODO Auto-generated method stub

	}

	public Set<Card> take(int count) {
		// TODO Auto-generated method stub
		return null;
	}

}
