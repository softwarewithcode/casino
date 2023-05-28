package com.casino.common.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Deck {
    private final List<Card> cards;

    private Deck() {
        cards = new ArrayList<>();
        for (int i = 1; i < 14; i++) {
            Card spade = Card.of(i, Suit.SPADE);
            cards.add(spade);
            Card diamond = Card.of(i, Suit.DIAMOND);
            cards.add(diamond);
            Card club = Card.of(i, Suit.CLUB);
            cards.add(club);
            Card heart = Card.of(i, Suit.HEART);
            cards.add(heart);
        }
    }

    public synchronized Card take() {
        return cards.remove(cards.size() - 1);
    }

    public synchronized List<Card> takeMany(int howMany) {
        List<Card> taken = new ArrayList<>();
        for (int i = 0; i < howMany; i++)
            taken.add(cards.remove(cards.size() - 1));
        return taken;
    }

    public List<Card> getCards() {
        return cards;
    }

    public static Deck createAndShuffle() {
        Deck deck = new Deck();
        Collections.shuffle(deck.getCards());
        return deck;
    }

    public static List<Card> pileUpAndShuffle(int deckCount) {
        List<Deck> decks = IntStream.range(0, deckCount).mapToObj(i -> new Deck()).toList();
        List<Card> cards = new ArrayList<>();
        decks.forEach(deck -> cards.addAll(deck.getCards()));
        Collections.shuffle(cards);
        return cards;
    }
}
