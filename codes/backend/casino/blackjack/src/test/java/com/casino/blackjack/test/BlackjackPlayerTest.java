package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;

public class BlackjackPlayerTest {
	@Test
	public void playerHoldsDealtCardsInFirstHand() {
		BlackjackPlayer player = new BlackjackPlayer("player", UUID.randomUUID(), BigDecimal.TEN);
		player.addCard(player.getHands().get(0), new Card(13, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(11, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
	}

	@Test
	public void handValueIsCalculatedCorrectly() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(7, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(8, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(15, values.get(0));
		Assertions.assertEquals(1, values.size());
	}

	@Test
	public void pictureCardsValuesAreCalculatedAsValueOfTen() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(12, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(13, Suit.CLUB));
		Assertions.assertEquals(20, player.getHands().get(0).calculateValues().get(0));
	}

	@Test
	public void initialAceCreatesTwoHandValues() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(1, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(2, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(3, values.get(0));
		Assertions.assertEquals(13, values.get(1));
	}

	@Test
	public void addedAceCreatesTwoValuesWhenTotalValueRemainsUnder22() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(5, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.addCard(player.getHands().get(0), new Card(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
	}

	@Test
	public void addedAceCreatesTwoValuesButGoesBackToOneValueWhenSecondValueGoesOver21() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), new Card(5, Suit.CLUB));
		player.addCard(player.getHands().get(0), new Card(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.addCard(player.getHands().get(0), new Card(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
		player.addCard(player.getHands().get(0), new Card(1, Suit.DIAMOND));
		values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(11, values.get(0));
		Assertions.assertEquals(21, values.get(1));
		player.addCard(player.getHands().get(0), new Card(1, Suit.DIAMOND));
		values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(12, values.get(0));
		Assertions.assertEquals(1, values.size());
	}

	private BlackjackPlayer createPlayer() {
		BlackjackPlayer player = new BlackjackPlayer("player", UUID.randomUUID(), BigDecimal.TEN);

		return player;
	}

	private Card createCard(int rank, Suit suit) {
		return new Card(rank, suit);
	}
}
