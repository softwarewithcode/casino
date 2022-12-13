package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
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
import com.casino.common.table.phase.GamePhase;
import com.casino.common.user.Bridge;

public class DealerTest extends BaseTest {
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
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			dealer = (BlackjackDealer) f.get(table);
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge3 = new Bridge("JohnDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			List<Card> cards = dealer.getDecks();
			cards.add(Card.of(4, Suit.CLUB));
			cards.add(Card.of(8, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.HEART));
			cards.add(Card.of(5, Suit.SPADE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void gamePhaseChangesFromBetToPlayAfterBetPhaseEnd() {
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.join(bridge, 1);
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(GamePhase.PLAY, table.getGamePhase());
	}

	@Test
	public void tableStatusChangesFromWaitingToRunningAfterFirstPlayerTakesSeat() {
		assertTrue(table.getStatus() == Status.WAITING_PLAYERS);
		assertTrue(table.join(bridge, 1));
		assertTrue(table.getStatus() == Status.RUNNING);
	}

	@Test
	public void twoCardsIsAddedToStartingHandAfterPlacingBet() {
		table.join(bridge, 6);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(bridge.playerId()).getHands().get(0).getCards().size());
	}

	@Test
	public void playersBalanceGetAdjustedAccordingToBet() {
		table.join(bridge, 1);
		table.join(bridge2, 2);
		table.bet(bridge.playerId(), new BigDecimal("54.0"));
		table.bet(bridge2.playerId(), new BigDecimal("51.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("946.00"), table.getPlayer(bridge.playerId()).getBalance());
		assertEquals(new BigDecimal("949.00"), table.getPlayer(bridge2.playerId()).getBalance());
	}

	@Test
	public void onlyPlayerWithBetReceivesStartingHand() {
		table.join(bridge, 1);
		table.join(bridge2, 2);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(2, table.getPlayer(bridge.playerId()).getHands().get(0).getCards().size());
		assertEquals(0, table.getPlayer(bridge2.playerId()).getHands().get(0).getCards().size());
	}

	@Test
	public void dealerCreatesEightDecks() {
		Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT,
				Type.PUBLIC);
		BlackjackDealer d = new BlackjackDealer(null, thresholds);
		Assertions.assertEquals(416, d.getDecks().size());
	}

	@Test
	public void betChangeDoesNotMakeOnTimeAndLastReceivedBetIsUsed() {
		table.join(bridge, 1);
		table.join(bridge2, 2);
		table.bet(bridge.playerId(), new BigDecimal("11.11"));
		table.bet(bridge2.playerId(), new BigDecimal("22.67"));
		table.bet(bridge.playerId(), new BigDecimal("44.55"));
		table.bet(bridge2.playerId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(bridge2.playerId(), new BigDecimal("99.0"));
		});
		assertEquals(new BigDecimal("44.55"), table.getPlayer(bridge.playerId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(bridge.playerId()).getTotalBet());
		assertEquals(new BigDecimal("955.45"), table.getPlayer(bridge.playerId()).getBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(bridge2.playerId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(bridge2.playerId()).getTotalBet());
		assertEquals(new BigDecimal("949.00"), table.getPlayer(bridge2.playerId()).getBalance());
	}

	@Test
	public void dealerDealsInOrderWithMultiplePlayers() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		table.join(bridge, 0);
		table.join(bridge2, 3);
		table.join(bridge3, 6);
		table.bet(bridge.playerId(), new BigDecimal("11.11"));
		table.bet(bridge2.playerId(), new BigDecimal("22.67"));
		table.bet(bridge3.playerId(), new BigDecimal("44.55"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(bridge.playerId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(6, table.getPlayer(bridge.playerId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(3, table.getPlayer(bridge2.playerId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(7, table.getPlayer(bridge2.playerId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(4, table.getPlayer(bridge3.playerId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(8, table.getPlayer(bridge3.playerId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(5, dealer.getHand().getCards().get(0).getRank());
		assertEquals(1, dealer.getHand().getCards().size());
	}

	@Test
	public void dealerChangesTurns() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.join(bridge, 0);
		table.join(bridge2, 3);
		table.join(bridge3, 6);
		assertFalse(table.isDealerTurn());
		table.bet(bridge.playerId(), new BigDecimal("11.11"));
		table.bet(bridge2.playerId(), new BigDecimal("22.67"));
		table.bet(bridge3.playerId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(bridge.playerId(), table.getPlayerInTurn().getId()); // equals compares UUID -> both have the same
		assertFalse(table.isDealerTurn());
		table.stand(bridge.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge2.playerId(), table.getPlayerInTurn().getId());
		assertFalse(table.isDealerTurn());
		table.stand(bridge2.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge3.playerId(), table.getPlayerInTurn().getId());
		table.stand(bridge3.playerId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}

	@Test
	public void dealerChangesTurnWhenPlayerGoesOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.join(bridge, 1);
		table.join(bridge2, 3);
		table.join(bridge3, 6);
		assertFalse(table.isDealerTurn());
		table.bet(bridge.playerId(), new BigDecimal("11.11"));
		table.bet(bridge2.playerId(), new BigDecimal("22.67"));
		table.bet(bridge3.playerId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(bridge.playerId(), table.getPlayerInTurn().getId());
		assertFalse(table.isDealerTurn());
		table.hit(bridge.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge2.playerId(), table.getPlayerInTurn().getId());
		assertFalse(table.isDealerTurn());
		table.hit(bridge2.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge3.playerId(), table.getPlayerInTurn().getId());
		table.hit(bridge3.playerId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}

	@Test
	public void dealerChangesAfterPlayerDoublesDown() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.join(bridge, 1);
		table.join(bridge2, 4);
		table.join(bridge3, 5);
		assertFalse(table.isDealerTurn());
		table.bet(bridge.playerId(), new BigDecimal("11.11"));
		table.bet(bridge2.playerId(), new BigDecimal("22.67"));
		table.bet(bridge3.playerId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(bridge.playerId(), table.getPlayerInTurn().getId());
		assertFalse(table.isDealerTurn());
		table.hit(bridge.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge2.playerId(), table.getPlayerInTurn().getId());
		assertFalse(table.isDealerTurn());
		table.hit(bridge2.playerId());
		assertFalse(table.isDealerTurn());
		assertEquals(bridge3.playerId(), table.getPlayerInTurn().getId()); // equals compares UUID
		table.hit(bridge3.playerId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}
}
