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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.table.InsuranceInfo;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

@Disabled
public class InsuranceTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID(),
					new InsuranceInfo(INSURANCE_ROUND_TIME_SECONDS));
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			List<Card> cards = dealer.getDecks();
			cards.add(Card.of(4, Suit.CLUB));
			cards.add(Card.of(8, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.DIAMOND));
			cards.add(Card.of(1, Suit.HEART));
			cards.add(Card.of(5, Suit.SPADE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void playerCanInsureHandWhenDealerHasStartingAce() {
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer);
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(blackjackPlayer.getActiveHand().isInsured());
	}

	@Test
	public void playerCannotInsureHandWhenDealerHasNotAce() {
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		dealer.getDecks().add(Card.of(2, Suit.HEART));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void playerWithoutBetCannotInsure() {
		table.trySeat(5, blackjackPlayer);
		table.trySeat(6, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer2);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void insuredHandCannotBeInsured() {
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.insure(blackjackPlayer);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.insure(blackjackPlayer);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void standNotAllowedDuringInsurancePhase() {
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		System.out.println("PHASE:" + table.getGamePhase() + " dealer:" + dealer.getHand().getCards().get(0));
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void doublingNotAllowedDuringInsurancePhase() {
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void splitNotAllowedDuringInsurancePhase() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		System.out.println("PHASE:" + table.getGamePhase() + " dealer:" + dealer.getHand().getCards().get(0));
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.splitStartingHand(blackjackPlayer);
		});
		sleep(INSURANCE_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
	}

	@Test
	public void insuredHandLosesInsuranceAndBetWhenDealerGets21() {
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(4, Suit.HEART));
		dealer.getDecks().add(Card.of(1, Suit.HEART));
		dealer.getDecks().add(Card.of(5, Suit.HEART));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(INSURANCE_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		table.takeCard(blackjackPlayer);
		assertEquals(10, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(20, blackjackPlayer.getActiveHand().calculateValues().get(1));
		table.stand(blackjackPlayer);
		assertEquals(21, dealer.getHand().getFinalValue());
		assertEquals(new BigDecimal("925.00"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("75.00"), blackjackPlayer.getTotalBet());
	}
}
