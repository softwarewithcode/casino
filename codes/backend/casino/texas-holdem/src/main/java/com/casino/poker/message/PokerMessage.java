package com.casino.poker.message;

import java.util.List;

import com.casino.common.cards.Card;
import com.casino.common.message.Message;

public class PokerMessage extends Message {

	private static final long serialVersionUID = 1L;

	private List<Card> cards;

	public PokerMessage(List<Card> holeCards) {
		super();
		this.cards = holeCards;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

}
