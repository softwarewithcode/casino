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

import com.casino.blackjack.dealer.Dealer;
import com.casino.blackjack.game.BlackjackData;
import com.casino.blackjack.game.BlackjackPhasePathFactory;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.language.Language;
import com.casino.common.game.Game;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;
import com.casino.common.user.User;

public class BalanceTest extends BaseTest {
	private BlackjackTable table;
	private User user;
	private User user2;
	private Dealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			TableThresholds thresholds = new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
			TableData tableInitData = new TableData(BlackjackPhasePathFactory.buildBlackjackPath(), TableStatus.WAITING_PLAYERS, thresholds, UUID.randomUUID(), Language.ENGLISH, TableType.MULTIPLAYER, Game.BLACKJACK);
			BlackjackData blackjackInitData = new BlackjackData(MIN_BUYIN, MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DEFAULT_ALLOWED_SIT_OUT_ROUNDS,
					DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS);
			table = new BlackjackTable(tableInitData, blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (Dealer) f.get(table);
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
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("990.00"), table.getPlayer(user.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("10.00"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(4, table.getPlayer(user.userId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(5, table.getPlayer(user.userId()).getActiveHand().getCards().get(1).getRank());
		table.doubleDown(user.userId());
		assertEquals(20, table.getPlayer(user.userId()).getHands().get(0).calculateFinalValue());
		assertEquals(new BigDecimal("20.00"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("1020.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void dealerCalculatesBalancesBasedOnLastAcceptedBets() {
		User blackjackPlayer = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("100"));
		User blackjackPlayer2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("100"));
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(11, Suit.SPADE));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(blackjackPlayer, "5");
		table.join(blackjackPlayer2, "6");
		table.bet(blackjackPlayer.userId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.userId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer.userId(), new BigDecimal("44.55"));
		table.bet(blackjackPlayer2.userId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(blackjackPlayer.userId());
		table.stand(blackjackPlayer2.userId());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.userId()).getTotalBet());
		assertEquals(new BigDecimal("144.55"), table.getPlayer(blackjackPlayer.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.userId()).getTotalBet());
		assertEquals(new BigDecimal("151.00"), table.getPlayer(blackjackPlayer2.userId()).getCurrentBalance());
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
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("949.90"), table.getPlayer(user.userId()).getCurrentBalance());
		table.split(user.userId());
		assertEquals(new BigDecimal("899.80"), table.getPlayer(user.userId()).getCurrentBalance());
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
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void totalBetIsUpdated() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(13, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1075.15"), table.getPlayer(user.userId()).getCurrentBalance());
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
		table.join(user, "5");
		table.join(user2, "6");
		table.bet(user.userId(), new BigDecimal("50.1"));
		table.bet(user2.userId(), new BigDecimal("12.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertTrue(table.isDealerTurn());
		assertEquals(new BigDecimal("949.90"), table.getPlayer(user.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1019.15"), table.getPlayer(user2.userId()).getCurrentBalance());
	}
}
