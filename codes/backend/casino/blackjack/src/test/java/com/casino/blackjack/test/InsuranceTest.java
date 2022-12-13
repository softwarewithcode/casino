package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.casino.common.user.Bridge;

public class InsuranceTest extends BaseTest {
	private BlackjackTable table;
//	private BlackjackPlayer blackjackPlayer;
//	private BlackjackPlayer blackjackPlayer2;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			List<Card> cards = dealer.getDecks();
			cards.add(Card.of(4, Suit.CLUB));
			cards.add(Card.of(8, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.CLUB));
			cards.add(Card.of(1, Suit.HEART));
			cards.add(Card.of(5, Suit.SPADE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void playerCanInsureHandWhenDealerHasStartingAce() {
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(bridge.playerId()).getActiveHand().isInsured());
	}

	@Test
	public void playerCannotInsureHandWhenDealerHasNotAce() {
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(bridge.playerId());
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void playerWithoutBetCannotInsure() {
		table.join(bridge, 5);
		table.join(bridge2, 6);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(bridge2.playerId());
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void insuredHandCannotBeInsured() {
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(bridge.playerId());
		});
	}

	@Test
	public void standNotAllowedDuringInsurancePhase() {
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(bridge.playerId());
		});
	}

	@Test
	public void doublingNotAllowedDuringInsurancePhase() {
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(bridge.playerId());
		});
	}

	@Test
	public void doublingAllowedAfterInsurancePhase() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.playerId());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(bridge.playerId());
		assertTrue(p.hasDoubled());
	}

	@Test
	public void splitNotAllowedDuringInsurancePhase() {
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		dealer.getDecks().add(Card.of(5, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(bridge.playerId());
		});
	}

	@Test
	public void insuredHandLosesInsuranceAndBetWhenDealerGets21() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(bridge.playerId());
		assertEquals(10, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(1));
		table.stand(bridge.playerId());
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		assertEquals(21, dealer.getHand().getFinalValue());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(bridge.playerId()).getTotalBet());
		assertEquals(new BigDecimal("925.00"), table.getPlayer(bridge.playerId()).getBalance());
	}

	@Test
	public void insuredHandLosesInsuranceButWinsBetWhenDealerGets19() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(8, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		table.hit(bridge.playerId());
		assertEquals(10, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(1));
		table.stand(bridge.playerId());
		assertEquals(19, dealer.getHand().getFinalValue());
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(bridge.playerId()).getTotalBet());
		assertEquals(new BigDecimal("1025.00"), table.getPlayer(bridge.playerId()).getBalance());
	}

	@Test
	public void insuredHandWinsInsuranceButLosesBetWhileHaving18() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.CLUB));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		table.hit(bridge.playerId());
		assertEquals(18, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(0));
		table.stand(bridge.playerId());
		assertTrue(dealer.getHand().isBlackjack());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(bridge.playerId());
		assertTrue(p.hasInsured());
		assertTrue(p.hasCompletedFirstHand());
		assertEquals(new BigDecimal("75.00"), p.getTotalBet());
		assertEquals(new BigDecimal("975.00"), p.getBalance());
	}

	@Test
	public void insuredHandProducesNothingIfHandGoesOver21AndDealerGetsBlackjack() {
		dealer.getDecks().add(Card.of(12, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		table.hit(bridge.playerId());
		assertEquals(18, table.getPlayer(bridge.playerId()).getActiveHand().calculateValues().get(0));
		table.hit(bridge.playerId());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(bridge.playerId());
		assertEquals(28, p.getFirstHandFinalValue());
		assertTrue(dealer.getHand().isBlackjack());
		assertTrue(table.getPlayer(bridge.playerId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(bridge.playerId()).getTotalBet());
		assertEquals(new BigDecimal("925.00"), table.getPlayer(bridge.playerId()).getBalance());
	}

	@Test
	public void insuredHandCannotBeSplit() {
		dealer.getDecks().add(Card.of(12, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(bridge, 5);
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(bridge.playerId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(bridge.playerId());
		});
	}
}
