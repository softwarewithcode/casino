package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.casino.blackjack.game.BlackjackData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.dealer.Dealer;
import com.casino.blackjack.export.BlackjackPlayerAction;
import com.casino.blackjack.player.BlackjackPlayer_;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.user.User;

public class InsuranceTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackTable table2;
	private Dealer dealer;
	private Dealer dealer2;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			BlackjackData blackjackInitData = createBlackjackInitData(MIN_BUYIN, MIN_BET, new BigDecimal("1000.0"), BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DEFAULT_ALLOWED_SIT_OUT_ROUNDS,
					DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS);
			table2 = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user3 = new User("JaneDoe2", table2.getId(), UUID.randomUUID(), null, new BigDecimal("450.0"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (Dealer) f.get(table);
			dealer2 = (Dealer) f.get(table2);
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

	private void joinBetInsure() {
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(user.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void playerCanInsureHandWhenDealerHasStartingAce() {
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(user.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(user.userId()).getActiveHand().isInsured());
	}

	@Test
	public void playerCannotInsureHandWhenDealerHasNotAce() {
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> table.insure(user.userId()));
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void playerWithoutBetCannotInsure() {
		table.join(user, "5");
		table.join(user2, "6");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(user.userId());
		assertThrows(IllegalPlayerActionException.class, () -> table.insure(user2.userId()));
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void insuredHandCannotBeInsured() {
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(user.userId());
		assertThrows(IllegalPlayerActionException.class, () -> table.insure(user.userId()));
	}

	@Test
	public void standNotAllowedDuringInsurancePhase() {
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> table.stand(user.userId()));
	}

	@Test
	public void doublingNotAllowedDuringInsurancePhase() {
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> table.doubleDown(user.userId()));
	}

	@Test
	public void doublingAllowedAfterInsurancePhase() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		BlackjackPlayer_ p = table.getPlayer(user.userId());
		assertTrue(p.hasDoubled());
	}

	@Test
	public void splitNotAllowedDuringInsurancePhase() {
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		dealer.getDecks().add(Card.of(5, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> table.split(user.userId()));
	}

	@Test
	public void insuredHandLosesInsuranceAndBetWhenDealerGets21() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(user.userId());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(user.userId());
		assertEquals(10, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(1));
		table.stand(user.userId());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		assertEquals(21, dealer.getHand().calculateFinalValue());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("925.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void insuredHandLosesInsuranceButWinsBetWhenDealerGets19() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(8, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.SPADE));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.CLUB));
		joinBetInsure();
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		table.hit(user.userId());
		assertEquals(10, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(20, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(1));
		table.stand(user.userId());
		assertEquals(19, dealer.getHand().calculateFinalValue());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("1025.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void insuredHandWinsInsuranceBetButLosesMainBetWhileHaving18() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.CLUB));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		joinBetInsure();
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		table.hit(user.userId());
		assertEquals(18, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.stand(user.userId());
		assertTrue(dealer.getHand().isBlackjack());
		BlackjackPlayer_ p = (BlackjackPlayer_) table.getPlayer(user.userId());
		assertTrue(p.hasInsured());
		assertTrue(p.hasCompletedFirstHand());
		assertEquals(new BigDecimal("75.00"), p.getTotalBet());
		assertEquals(new BigDecimal("975.00"), p.getCurrentBalance());
	}

	@Test
	public void insuredHandPaysInsuranceBetEvenIfHandGoesOver21AndDealerGetsBlackjack() {
		dealer.getDecks().add(Card.of(12, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		joinBetInsure();
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		table.hit(user.userId());
		assertEquals(18, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.hit(user.userId());
		BlackjackPlayer_ p = (BlackjackPlayer_) table.getPlayer(user.userId());
		assertEquals(28, p.getFirstHandFinalValue());
		assertTrue(dealer.getHand().isBlackjack());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isInsured());
		assertEquals(new BigDecimal("75.00"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("975.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void insuredHandCannotBeSplit() {
		dealer.getDecks().add(Card.of(12, Suit.HEART));
		dealer.getDecks().add(Card.of(13, Suit.HEART));
		dealer.getDecks().add(Card.of(9, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		joinBetInsure();
		assertThrows(IllegalPlayerActionException.class, () -> table.split(user.userId()));
	}

	@Test
	public void playerJoinsDuringInsurancePhaseAndDoesNotGetCardsAndHaveNoWinningChance() {
		dealer.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(9, Suit.DIAMOND));
		dealer.getDecks().add(Card.of(1, Suit.SPADE));
		dealer.getDecks().add(Card.of(10, Suit.HEART));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.join(user2, "3");
		table.insure(user.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		sleep(ONE_UNIT, ChronoUnit.SECONDS);
		assertEquals(0, table.getPlayer(user2.userId()).getHands().get(0).getCards().size());
		assertEquals(17, table.getDealerHand().calculateFinalValue());
		assertEquals(19, table.getPlayer(user.userId()).getHands().get(0).calculateFinalValue());
	}

	@Test
	public void insuredHandLosesByTimeoutAndBalanceIsUpdated() {
		dealer2.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(3, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(12, table2.getPlayer(user3.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(1, table2.getDealerHand().calculateValues().get(0));
		assertEquals(11, table2.getDealerHand().calculateValues().get(1));
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("300.00"), table2.getPlayer(user3.userId()).getCurrentBalance());
	}

	@Test
	public void insuredHandWinsByTimeoutAndBalanceIsUpdated() {
		dealer2.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(11, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("500.00"), table2.getPlayer(user3.userId()).getCurrentBalance());
	}

	@Test
	public void onlyInsuranceBetGetsPaidWhenGoingOver21() {
		dealer2.getDecks().add(Card.of(10, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(11, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.hit(user3.userId());
		assertEquals(new BigDecimal("400.00"), table2.getPlayer(user3.userId()).getCurrentBalance());
	}

	@Test
	public void splitIsNotAnOptionWhenHandIsInsured() {
		dealer2.getDecks().add(Card.of(10, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(11, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		assertTrue(table2.getPlayer(user3.userId()).getHands().get(0).isInsured());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table2.getPlayer(user3.userId()).getActions().stream().filter(action -> action == BlackjackPlayerAction.SPLIT).findAny().isEmpty());
	}

	@Test
	public void doubleDownIsAnOptionWithInsuredHand() {
		dealer2.getDecks().add(Card.of(10, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(6, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(2, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		assertTrue(table2.getPlayer(user3.userId()).getHands().get(0).isInsured());
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table2.getPlayer(user3.userId()).getActions().stream().filter(action -> action == BlackjackPlayerAction.DOUBLE_DOWN).findAny().isPresent());
	}

	@Test
	public void insureIsAnOptionWhenPlayerHasLeftHalfOfTheTotalBet() {
		dealer2.getDecks().add(Card.of(2, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("300.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table2.insure(user3.userId());
		assertTrue(table2.getPlayer(user3.userId()).getHands().get(0).isInsured());
	}

	@Test
	public void insureNotAnOptionWhenPlayerHasLeftLessThanHalfOfTheTotalBet() {
		dealer2.getDecks().add(Card.of(2, Suit.DIAMOND));
		dealer2.getDecks().add(Card.of(1, Suit.CLUB));
		dealer2.getDecks().add(Card.of(9, Suit.SPADE));
		table2.join(user3, "5");
		table2.bet(user3.userId(), new BigDecimal("300.01"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertFalse(table2.getPlayer(user3.userId()).getHands().get(0).isInsured());
		assertThrows(IllegalPlayerActionException.class, () -> table2.insure(user3.userId()));
	}

}
