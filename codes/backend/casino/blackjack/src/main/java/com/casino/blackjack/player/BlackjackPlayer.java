package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.casino.blackjack.util.AcePredicate;
import com.casino.blackjack.util.HighCardPredicate;
import com.casino.common.cards.Card;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.Seat;

public class BlackjackPlayer extends CasinoPlayer {
	private List<Card> cards;
	private Set<Seat> seats;

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

	@Override
	public void onLeave() {
		super.onLeave();
		if (seats == null)
			return;
//		seats.stream().
	}
}
