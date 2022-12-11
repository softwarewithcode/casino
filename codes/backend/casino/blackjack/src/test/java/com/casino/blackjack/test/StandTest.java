package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class StandTest extends BaseTest {
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
	public void onlyOnePlayerReactsOnTimeButAllWinsBecauseDealerGetsOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));// dealer
		cards.add(Card.of(1, Suit.HEART));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.SPADE));
		table.join(0, blackjackPlayer);
		table.join(2, blackjackPlayer2);
		table.join(6, blackjackPlayer3);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		table.bet(blackjackPlayer2, new BigDecimal("10.0"));
		table.bet(blackjackPlayer3, new BigDecimal("25.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer);
		sleep(PLAYER_TIME_SECONDS * 3 + 1, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1099.00"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("1015.00"), blackjackPlayer2.getBalance());
		assertEquals(new BigDecimal("1037.50"), blackjackPlayer3.getBalance());
	}

	@Test
	public void dealerGetsTurnAfterLastPlayerCallsStand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.join(6, blackjackPlayer2);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		table.bet(blackjackPlayer2, new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		table.stand(blackjackPlayer2);
		assertTrue(table.isDealerTurn());
	}

	@Test
	public void callingStandInAWrongTurnResultsToException() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.join(6, blackjackPlayer2);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		table.bet(blackjackPlayer2, new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer2);
		});
	}

	@Test
	public void callingStandWhenHandIsCompletedResultsToException() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer);
		});
	}

	@Test
	public void callingStandWhileHavingBlackjackIsNotAllowedAsHandIsCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertNull(blackjackPlayer.getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer);
		});
	}

	@Test
	public void callingStandOnSecondHandOfSplitCompletesSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		table.hit(blackjackPlayer);
		table.stand(blackjackPlayer);
		table.hit(blackjackPlayer);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(blackjackPlayer.getHands().get(1).isCompleted());
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn17() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(17, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToCompleteRound() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertTrue(dealer.isRoundCompleted());
		assertEquals(17, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn18() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(18, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn19() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(19, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn20() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(20, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(21, dealer.getHand().calculateValues().get(0));
	}
}
