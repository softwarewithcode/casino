package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.user.Bridge;

public class BalanceTest extends BaseTest {
	private BlackjackTable table;
	private Bridge bridge;
	private Bridge bridge2;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void doublingUpdatesBalance() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("990.00"), table.getPlayer(bridge.userId()).getBalance());
		assertEquals(new BigDecimal("10.00"), table.getPlayer(bridge.userId()).getTotalBet());
		assertEquals(4, table.getPlayer(bridge.userId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(5, table.getPlayer(bridge.userId()).getActiveHand().getCards().get(1).getRank());
		table.doubleDown(bridge.userId());
		assertEquals(20, table.getPlayer(bridge.userId()).getHands().get(0).calculateFinalValue());
		assertEquals(new BigDecimal("20.00"), table.getPlayer(bridge.userId()).getTotalBet());
		assertEquals(new BigDecimal("1020.00"), table.getPlayer(bridge.userId()).getBalance());
	}

	@Test
	public void dealerCalculatesBalancesBasedOnLastAcceptedBets() {
		Bridge blackjackPlayer = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("100"));
		Bridge blackjackPlayer2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("100"));
		table.join(blackjackPlayer, "5");
		table.join(blackjackPlayer2, "6");
		table.bet(blackjackPlayer.userId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.userId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer.userId(), new BigDecimal("44.55"));
		table.bet(blackjackPlayer2.userId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.userId()).getTotalBet());
		assertEquals(new BigDecimal("55.45"), table.getPlayer(blackjackPlayer.userId()).getBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.userId()).getTotalBet());
		assertEquals(new BigDecimal("49.00"), table.getPlayer(blackjackPlayer2.userId()).getBalance());
	}

	@Test
	public void splitReducesTotalBalance() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("949.90"), table.getPlayer(bridge.userId()).getBalance());
		table.split(bridge.userId());
		assertEquals(new BigDecimal("899.80"), table.getPlayer(bridge.userId()).getBalance());
	}

	@Test
	public void playerWinsWithBlackjackAndBalanceIsUpdatedWithBlackjackFactor() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(13, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), table.getPlayer(bridge.userId()).getBalance());
	}
	
	@Test
	public void totalBetIsUpdated() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(13, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), table.getPlayer(bridge.userId()).getBalance());
	}

	@Test
	public void secondPlayerWinsWithBlackjackAndTurnIsChangedToDealer() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.HEART));
		cards.add(Card.of(11, Suit.SPADE));
		table.join(bridge, "5");
		table.join(bridge2, "6");
		table.bet(bridge.userId(), new BigDecimal("50.1"));
		table.bet(bridge2.userId(), new BigDecimal("12.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(bridge.userId());
		assertTrue(table.isDealerTurn());
		assertEquals(new BigDecimal("949.90"), table.getPlayer(bridge.userId()).getBalance());
		assertEquals(new BigDecimal("1019.15"), table.getPlayer(bridge2.userId()).getBalance());
	}
}
