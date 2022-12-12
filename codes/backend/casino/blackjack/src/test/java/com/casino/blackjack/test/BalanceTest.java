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

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class BalanceTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	private BlackjackPlayer blackjackPlayer3;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer3 = new BlackjackPlayer("JaneDoe2", UUID.randomUUID(), new BigDecimal("1000"), table);
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("990.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("10.00"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(4, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(5, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().get(1).getRank());
		table.doubleDown(table.getPlayer(blackjackPlayer.getId()).getId());
		assertEquals(20, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getFinalValue());
		assertEquals(new BigDecimal("20.00"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("1020.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
	}

	@Test
	public void dealerCalculatesBalancesBasedOnLastAcceptedBets() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer.getId(), new BigDecimal("44.55"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("55.45"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.getId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.getId()).getTotalBet());
		assertEquals(new BigDecimal("49.00"), table.getPlayer(blackjackPlayer2.getId()).getBalance());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("949.90"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		table.split(blackjackPlayer.getId());
		assertEquals(new BigDecimal("899.80"), table.getPlayer(blackjackPlayer.getId()).getBalance());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), table.getPlayer(blackjackPlayer.getId()).getBalance());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.1"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("12.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertTrue(table.isDealerTurn());
		assertEquals(new BigDecimal("949.90"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("1019.15"), table.getPlayer(blackjackPlayer2.getId()).getBalance());
	}
}
