package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.casino.blackjack.util.AcePredicate;
import com.casino.blackjack.util.HighCardPredicate;
import com.casino.common.cards.Card;
import com.casino.common.player.BasePlayer;

public class BlackjackPlayer extends BasePlayer {
	private List<Card> cards;

	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, BigDecimal endBalance) {
		super(name, id, startBalance, endBalance);
		cards = new ArrayList<Card>();
	}

	public void addCard(Card card) {
		cards.add(card);
	}

	public List<Integer> calculateSums() {
		HighCardPredicate<Card> highCardPredicate = new HighCardPredicate<Card>();
		AcePredicate<Card> acePredicate = new AcePredicate<>();
		// return cards.stream().filter(highCardPredicate);
		return new ArrayList();
	}

	public List<Card> getCards() {
		return cards;
	}
}
