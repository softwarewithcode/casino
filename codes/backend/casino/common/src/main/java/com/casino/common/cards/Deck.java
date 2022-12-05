package com.casino.common.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Deck implements IDeck {
	private Set<Card> cards;

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

	public Set<Card> getCards() {
		return cards;
	}

	public Set<Card> take(int count) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<Card> combineDecks(int count) {
		List<Deck> decks = IntStream.range(0, count).mapToObj(i -> new Deck()).collect(Collectors.toList());
		List<Card> cards = new ArrayList<Card>();
		decks.forEach(deck -> {
			cards.addAll(deck.getCards());
		});
		Collections.shuffle(cards);
		return cards;
	}
}
