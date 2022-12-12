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

public class InsuranceTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getActiveHand().isInsured());
	}

	@Test
	public void playerCannotInsureHandWhenDealerHasNotAce() {
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer.getId());
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void playerWithoutBetCannotInsure() {
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer2.getId());
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void insuredHandCannotBeInsured() {
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer.getId());
		});
	}

	@Test
	public void standNotAllowedDuringInsurancePhase() {
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer.getId());
		});
	}

	@Test
	public void doublingNotAllowedDuringInsurancePhase() {
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(blackjackPlayer.getId());
		assertTrue(p.hasDoubled());
	}

	@Test
	public void splitNotAllowedDuringInsurancePhase() {
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		dealer.getDecks().add(Card.of(5, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer.getId());
		assertEquals(10, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(1));
		table.stand(blackjackPlayer.getId());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		assertEquals(21, dealer.getHand().getFinalValue());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("925.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
	}

	@Test
	public void insuredHandLosesInsuranceButWinsBetWhenDealerGets19() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(8, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		table.hit(blackjackPlayer.getId());
		assertEquals(10, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(1));
		table.stand(blackjackPlayer.getId());
		assertEquals(19, dealer.getHand().getFinalValue());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("1025.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
	}

	@Test
	public void insuredHandWinsInsuranceButLosesBetWhileHaving18() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.CLUB));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		table.hit(blackjackPlayer.getId());
		assertEquals(18, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		table.stand(blackjackPlayer.getId());
		assertTrue(dealer.getHand().isBlackjack());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		table.hit(blackjackPlayer.getId());
		assertEquals(18, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		table.hit(blackjackPlayer.getId());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(blackjackPlayer.getId());
		assertEquals(28, p.getFirstHandFinalValue());
		assertTrue(dealer.getHand().isBlackjack());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("925.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
	}

	@Test
	public void insuredHandCannotBeSplit() {
		dealer.getDecks().add(Card.of(12, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer.getId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
		});
	}
}
