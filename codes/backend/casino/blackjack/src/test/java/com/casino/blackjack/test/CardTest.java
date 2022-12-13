package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.user.Bridge;

public class CardTest extends BaseTest {

	@BeforeEach
	public void initTest() {
		try {
			BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void handValueIsCalculatedCorrectly() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), Card.of(7, Suit.CLUB));
		player.addCard(player.getHands().get(0), Card.of(8, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(15, values.get(0));
		Assertions.assertEquals(1, values.size());
	}

	@Test
	public void pictureCardsValuesAreCalculatedAsValueOfTen() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), Card.of(12, Suit.CLUB));
		player.addCard(player.getHands().get(0), Card.of(13, Suit.CLUB));
		Assertions.assertEquals(20, player.getHands().get(0).calculateValues().get(0));
	}

	@Test
	public void initialAceCreatesTwoHandValues() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), Card.of(1, Suit.CLUB));
		player.addCard(player.getHands().get(0), Card.of(2, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(3, values.get(0));
		Assertions.assertEquals(13, values.get(1));
	}

	@Test
	public void addedAceCreatesTwoValuesWhenTotalValueRemainsUnder22() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), Card.of(5, Suit.CLUB));
		player.addCard(player.getHands().get(0), Card.of(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.addCard(player.getHands().get(0), Card.of(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
	}

	@Test
	public void addedAceCreatesCompletesHandWhen21Reached() {
		BlackjackPlayer player = createPlayer();
		player.addCard(player.getHands().get(0), Card.of(5, Suit.CLUB));
		player.addCard(player.getHands().get(0), Card.of(4, Suit.CLUB));
		Assertions.assertEquals(1, player.getHands().size());
		player.addCard(player.getHands().get(0), Card.of(1, Suit.CLUB));
		List<Integer> values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(10, values.get(0));
		Assertions.assertEquals(20, values.get(1));
		player.addCard(player.getHands().get(0), Card.of(1, Suit.DIAMOND));
		values = player.getHands().get(0).calculateValues();
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(11, values.get(0));
		Assertions.assertEquals(21, values.get(1));
		Assertions.assertTrue(player.getHands().get(0).isCompleted());
	}

	private BlackjackPlayer createPlayer() {
		BlackjackPlayer player = new BlackjackPlayer(bridge, null);

		return player;
	}
}
