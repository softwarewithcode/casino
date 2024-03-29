package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer_;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.user.User;

public class CardTest extends BaseTest {
	BlackjackTable table;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void handValueIsCalculatedCorrectly() {
		BlackjackPlayer_ player = createPlayer();
		player.hit(Card.of(7, Suit.CLUB));
		player.hit(Card.of(8, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(15, values.get(0));
		Assertions.assertEquals(1, values.size());
	}

	@Test
	public void pictureCardsValuesAreCalculatedAsValueOfTen() {
		BlackjackPlayer_ player = createPlayer();
		player.hit(Card.of(12, Suit.CLUB));
		player.hit(Card.of(13, Suit.CLUB));
		Assertions.assertEquals(20, player.getHands().get(0).calculateValues().get(0));
	}

	@Test
	public void initialAceCreatesTwoHandValues() {
		BlackjackPlayer_ player = createPlayer();
		player.hit(Card.of(1, Suit.CLUB));
		player.hit(Card.of(2, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(3, values.get(0));
		Assertions.assertEquals(13, values.get(1));
	}

	@Test
	public void addedAceCreatesTwoValuesWhenTotalValueRemainsUnder22() {
		BlackjackPlayer_ player = createPlayer();
		player.hit(Card.of(5, Suit.CLUB));
		player.hit(Card.of(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.hit(Card.of(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
	}

	@Test
	public void addedAceCreatesCompletesHandWhen21Reached() {
		BlackjackPlayer_ player = createPlayer();
		player.hit(Card.of(5, Suit.CLUB));
		player.hit(Card.of(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.hit(Card.of(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
		player.hit(Card.of(1, Suit.DIAMOND));
		values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(11, values.get(0));
		Assertions.assertEquals(21, values.get(1));
		Assertions.assertTrue(player.getHands().get(0).isCompleted());
	}

	private BlackjackPlayer_ createPlayer() {
		BlackjackPlayer_ player = new BlackjackPlayer_(user, table);

		return player;
	}
}
