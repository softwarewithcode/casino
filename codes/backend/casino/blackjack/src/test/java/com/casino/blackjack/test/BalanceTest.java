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
import com.casino.blackjack.table.InsuranceInfo;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
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
			table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID(), new InsuranceInfo(5));
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
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("990.00"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("10.00"), blackjackPlayer.getTotalBet());
		assertEquals(4, blackjackPlayer.getActiveHand().getCards().get(0).getRank());
		assertEquals(5, blackjackPlayer.getActiveHand().getCards().get(1).getRank());
		table.doubleDown(blackjackPlayer);
		assertEquals(20, blackjackPlayer.getHands().get(0).getFinalValue());
		assertEquals(new BigDecimal("20.00"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("1020.00"), blackjackPlayer.getBalance());
	}

	@Test
	public void dealerCalculatesBalancesBasedOnLastAcceptedBets() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		table.trySeat(5, blackjackPlayer);
		table.trySeat(6, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer, new BigDecimal("44.55"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("55.45"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getTotalBet());
		assertEquals(new BigDecimal("49.00"), blackjackPlayer2.getBalance());
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
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("949.90"), blackjackPlayer.getBalance());
		table.splitStartingHand(blackjackPlayer);
		assertEquals(new BigDecimal("899.80"), blackjackPlayer.getBalance());
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
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), blackjackPlayer.getBalance());
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
		table.trySeat(5, blackjackPlayer);
		table.trySeat(6, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.1"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("12.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer);
		assertTrue(table.isDealerTurn());
		assertEquals(new BigDecimal("949.90"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("1019.15"), blackjackPlayer2.getBalance());
	}
}
