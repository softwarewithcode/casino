package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;

public class BlackjackPlayerTest {
	@Test
	public void playerHoldsDealtCardsInFirstHand() {
		BlackjackPlayer player = new BlackjackPlayer("player", UUID.randomUUID(), BigDecimal.TEN, null);
		player.addCard(player.getHands().get(0), new Card(13, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(11, Suit.CLUB));
		Assertions.assertEquals(2, player.getHands().size());
	}

	@Test
	public void handValueIsCorrect() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(7, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(8, Suit.CLUB));
		Assertions.assertEquals(15, player.getHands().get(0).calculateValue());
	}

	@Test
	public void pictureCardsAreValuedAsRankOfTen() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(12, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(13, Suit.CLUB));
		Assertions.assertEquals(20, player.getHands().get(0).calculateValue());
	}

	private BlackjackPlayer createPlayer() {
		BlackjackPlayer player = new BlackjackPlayer("player", UUID.randomUUID(), BigDecimal.TEN, null);

		return player;
	}

	private Card createCard(int rank, Suit suit) {
		return new Card(rank, suit);
	}
}
