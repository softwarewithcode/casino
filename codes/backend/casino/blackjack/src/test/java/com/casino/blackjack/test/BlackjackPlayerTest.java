package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackPlayerTest {
	@Test
	public void userHoldsDealtCards() {
		BlackjackPlayer player = new BlackjackPlayer("player", UUID.randomUUID(), BigDecimal.TEN, null);
		player.addCard(createCard(13, Suit.CLUB));
		player.addCard(createCard(11, Suit.CLUB));
		Assertions.assertEquals(2, player.getCards().size());
	}

	@Test
	public void calculateSumOfBasicCards() {
		;
	}

	private Card createCard(int rank, Suit suit) {
		return new Card(rank, suit);
	}
}
