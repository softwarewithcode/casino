package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.external.IBlackjackTable;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class StartingHandDoubleTest extends BaseTest {
	private IBlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackDealer dealer;
	private BigDecimal initialBet = new BigDecimal("25.78");

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoes", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void doublingNineResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer);
		assertEquals(new BigDecimal("51.56").setScale(2), blackjackPlayer.getTotalBet());
	}

	@Test
	public void doublingTenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer);
		assertEquals(new BigDecimal("51.56"), blackjackPlayer.getTotalBet());
	}

	@Test
	public void doublingElevenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer);
		assertEquals(new BigDecimal("51.56"), blackjackPlayer.getTotalBet());
	}

	@Test
	public void doublingUpdatesBalance() {
		table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(new BigDecimal("0.01"), MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("0.01234"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("999.98766"), blackjackPlayer.getBalance());
		table.doubleDown(blackjackPlayer);
		assertEquals(new BigDecimal("0.02468"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("999.97532"), blackjackPlayer.getBalance());
	}

	@Test
	public void doublingIsAllowedOnlyOnce() {
		table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(new BigDecimal("0.01"), MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("0.01234"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("999.98766"), blackjackPlayer.getBalance());
		table.doubleDown(blackjackPlayer);
		assertEquals(new BigDecimal("0.02468"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("999.97532"), blackjackPlayer.getBalance());
		IllegalPlayerActionException exception = assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer);
		});
		assertEquals(10, exception.getCode());
	}

}
