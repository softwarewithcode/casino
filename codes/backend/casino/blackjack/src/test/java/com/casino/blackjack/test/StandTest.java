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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 2);
		table.join(blackjackPlayer3.getId(), blackjackPlayer3.getName(), blackjackPlayer3.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("10.0"));
		table.bet(blackjackPlayer3.getId(), new BigDecimal("25.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer.getId());
		sleep(PLAYER_TIME_SECONDS * 3 + 1, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1099.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("1015.00"), table.getPlayer(blackjackPlayer2.getId()).getBalance());
		assertEquals(new BigDecimal("1037.50"), table.getPlayer(blackjackPlayer3.getId()).getBalance());
	}

	@Test
	public void dealerGetsTurnAfterLastPlayerCallsStand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		table.stand(blackjackPlayer2.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer2.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		table.hit(blackjackPlayer.getId());
		table.stand(blackjackPlayer.getId());
		table.hit(blackjackPlayer.getId());
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(1).isCompleted());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(21, dealer.getHand().calculateValues().get(0));
	}
}
