package com.casino.common.dealer;

import java.util.List;

import com.casino.common.cards.Card;
import com.casino.common.cards.CardHand;

public interface CardDealer extends Croupier {
    default void dealCard(List<Card> deck, CardHand hand) {
        Card card = removeLastCardFromDeck(deck);
        hand.addCard(card);
    }

    default Card removeLastCardFromDeck(List<Card> deck) {
        return deck.remove(deck.size() - 1);
    }


}
