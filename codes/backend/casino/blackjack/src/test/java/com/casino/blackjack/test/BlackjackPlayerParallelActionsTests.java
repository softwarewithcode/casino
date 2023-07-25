package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.user.User;

/**
 * 
 * 12/7/2022 <br>
 * This class uses preview feature from JDK 19.<br>
 * Running these tests requires VM argument --enable-preview. <br>
 * One approach In Eclipse:<br>
 * -> Mouse right click on top of this file <br>
 * -> "run as" -> "run configurations.." -> "Arguments tab" <br>
 * -> Use VM Arguments -> "-ea --enable-preview" <br>
 * With other IDEs something similar.
 */

//01/14/2023 -> requires jdk19
public class BlackjackPlayerParallelActionsTests extends BaseTest {
	private BlackjackTable table;
	private BlackjackTable tableWith49MinAnd49MaxPlayers;
	BlackjackPlayer doubleDownPlayer;
	private BlackjackDealer dealer;
	private volatile int playersWhoGotSeat;
	private volatile int playerWhoDidNotGetSeat;
	private volatile int rejectedInsurances;
	private volatile int rejectedDoubles;
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(BlackjackPlayerParallelActionsTests.class.getName());
	private User user3;
	private TableData tableInitData49Players;
	private BlackjackTable table49Players;

	@BeforeEach
	public void initTest() {
		try {
			TableThresholds thresholds = new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
			TableData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
			table = new BlackjackTable(tableInitData, blackjackInitData);
			tableInitData49Players = getDefaultTableInitDataWithThresholds(new TableThresholds(49, 49, 49));
			tableWith49MinAnd49MaxPlayers = new BlackjackTable(tableInitData49Players, blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user3 = new User("qq", table.getId(), UUID.randomUUID(), null, new BigDecimal("10000000.0"));
			doubleDownPlayer = new BlackjackPlayer(user3, table);
			playerWhoDidNotGetSeat = 0;
			playersWhoGotSeat = 0;
			rejectedDoubles = 0;
			rejectedInsurances = 0;
			playersWhoGotSeat = 0;
			playerWhoDidNotGetSeat = 0;
			BlackjackDealer dealer = getDealer(table);
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

	private BlackjackDealer getDealer(BlackjackTable table) {
		try {
			Field f;
			f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			return (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dealer;
	}

	private synchronized void addRejected() {
		playerWhoDidNotGetSeat++;
	}

	private synchronized void addAccepted() {
		playersWhoGotSeat++;
	}

	private synchronized void updateRejectedInsuranceCount() {
		rejectedInsurances++;
	}

	private synchronized void updateRejectedDoubleDowns() {
		rejectedDoubles++;
	}

	@Test
	public void threeOutOfTenConcurrentPlayersDontGetSeatInASevenSeatedTable() throws InterruptedException, BrokenBarrierException {
		CyclicBarrier casinoBarrier = new CyclicBarrier(11);
		List<Thread> threads = createCasinoPlayers(10, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(2, ChronoUnit.SECONDS);// MainThread waits 2 seconds for VirtualThreads to finish
		assertEquals(3, playerWhoDidNotGetSeat);
		assertEquals(7, playersWhoGotSeat);
	}

	@Test
	public void fiftyOneOutOfHundredPlayersGetRejectedAnd49GetAcceptedIn49SeatedTable() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(tableInitData49Players, blackjackInitData);
		CyclicBarrier casinoBarrier = new CyclicBarrier(101);
		List<Thread> threads = createCasinoPlayers(100, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(4, ChronoUnit.SECONDS);
		assertEquals(49, playersWhoGotSeat);
		assertEquals(51, playerWhoDidNotGetSeat);
//		assertEquals(49, table.getReservedSeatCount());
	}

	@Test
	public void insuranceCanBetSetOnlyOnceByPlayer() throws InterruptedException, BrokenBarrierException {
		table = tableWith49MinAnd49MaxPlayers;
		List<Card> cards = getDealer(table).getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));// Dealer's ace
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		CyclicBarrier casinoBarrier = new CyclicBarrier(29);
		List<Thread> threads = sendSimultaneouslyMultipleInsuranceRequestsForSpecificPlayer(28, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(BET_ROUND_TIME_SECONDS + INSURANCE_ROUND_TIME_SECONDS + 3, ChronoUnit.SECONDS);
		assertEquals(27, rejectedInsurances);
	}

	@Test
	public void doubleDownCanDoneOnlyOnceDirectlyCallingHand() throws InterruptedException, BrokenBarrierException {
		table = tableWith49MinAnd49MaxPlayers;
		CyclicBarrier casinoBarrier = new CyclicBarrier(1001);
		List<Thread> threads = createDoubleDownThreads(1000, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(1, ChronoUnit.SECONDS);
		assertEquals(999, rejectedDoubles);
	}

	@Test
	public void tableAcceptsWatchersSimultaneously() throws InterruptedException, BrokenBarrierException {
		CyclicBarrier casinoBarrier = new CyclicBarrier(10001);
		tableWith49MinAnd49MaxPlayers.join(user, "3");
		assertEquals(0, tableWith49MinAnd49MaxPlayers.getWatchers().size());
		List<Thread> threads = addWatchersToTable(10000, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(1, ChronoUnit.SECONDS); // All watchers have had 1 second of time to join.
		tableWith49MinAnd49MaxPlayers.join(user2, "4");
		assertEquals(10000, table.getWatchers().size());
		sleep(tableWith49MinAnd49MaxPlayers.getDealer().getBetPhaseTime(), ChronoUnit.SECONDS);
	}

	@Test
	public void doubleDownCanBeDoneOnlyOnceByPlayer() throws InterruptedException, BrokenBarrierException {
		table = tableWith49MinAnd49MaxPlayers;
		List<Card> cards = getDealer(table).getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));// Double card for player2
		cards.add(Card.of(5, Suit.DIAMOND));// Double card for player1
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(7, Suit.HEART));// Player2 second
		cards.add(Card.of(5, Suit.DIAMOND));// Player1 second
		cards.add(Card.of(1, Suit.DIAMOND));// Dealer's card
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(9, Suit.CLUB));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.HEART)); // player2 first card
		cards.add(Card.of(5, Suit.SPADE));// player1 first card
		CyclicBarrier casinoBarrier = new CyclicBarrier(29);
		List<Thread> threads = sendSimultaneouslyMultipleDoubleDownsFromPlayers(28, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		int waitSecondsForPlayersToFinish = 9;
		sleep(BET_ROUND_TIME_SECONDS + waitSecondsForPlayersToFinish + 3, ChronoUnit.SECONDS);
		BlackjackPlayer b = (BlackjackPlayer) table.getPlayer(0);
		assertEquals(15, b.getFirstHandFinalValue());
		assertEquals(MAX_BET.multiply(BigDecimal.TWO).setScale(2), table.getPlayer(0).getTotalBet());
		assertEquals(MAX_BET.multiply(BigDecimal.TWO).setScale(2), b.getBet(0));
		assertTrue(b.hasDoubled());
		assertEquals(27, rejectedDoubles);
	}

	private List<Thread> sendSimultaneouslyMultipleDoubleDownsFromPlayers(int playerAmount, CyclicBarrier casinoDoor) {
		UUID uuid = UUID.randomUUID();
		User doublerUser = new User("player:" + 0, table.getId(), uuid, null, new BigDecimal("10000000.0"));
//		BlackjackPlayer doubler1 = new BlackjackPlayer(bridge3, table);
		return IntStream.rangeClosed(0, playerAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			User b = null;
			if (index > 0)

				b = new User("player:" + index, table.getId(), UUID.randomUUID(), null, new BigDecimal("10000000.0"));
			try {
				int seatNumber = index;
				casinoDoor.await();
				if (index == 0) {
					table.join(doublerUser, "0");
					table.bet(doublerUser.userId(), MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS + 2, ChronoUnit.SECONDS);
					table.doubleDown(doublerUser.userId());
				} else {
					table.join(b, String.valueOf(seatNumber));
					table.bet(b.userId(), MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS + 2, ChronoUnit.SECONDS);
					table.doubleDown(doublerUser.userId());//// All these players try to doubleDown for the first player
				}

			} catch (Exception e) {
				LOGGER.log(Level.INFO, "doubling down excption:", e);
				updateRejectedDoubleDowns();
			}
		})).toList();
	}

	private List<Thread> sendSimultaneouslyMultipleInsuranceRequestsForSpecificPlayer(int amount, CyclicBarrier casinoDoor) {
		UUID uuid = UUID.randomUUID();
		User insurerUser = new User("player:" + 0, table.getId(), uuid, null, new BigDecimal("10000000.0"));
		return IntStream.rangeClosed(0, amount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			User b = null;
			if (index > 0)
				b = new User("player:" + index, table.getId(), UUID.randomUUID(), null, new BigDecimal("10000000.0"));
			try {
				int seatNumber = index;
				casinoDoor.await();
				if (index == 0) {
					table.join(insurerUser, String.valueOf(seatNumber));
					table.bet(insurerUser.userId(), MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
					table.insure(insurerUser.userId());
				} else {
					table.join(b, String.valueOf(seatNumber));
					table.bet(b.userId(), MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
					table.insure(insurerUser.userId());
				}

			} catch (Exception e) {
				LOGGER.log(Level.INFO, "insurance excption:", e);
				updateRejectedInsuranceCount();
			}
		})).toList();
	}

	private List<Thread> createDoubleDownThreads(int threadAmount, CyclicBarrier barrier) {
		BlackjackHand hand = (BlackjackHand) doubleDownPlayer.getActiveHand();
		hand.addCard(Card.of(2, Suit.CLUB));
		hand.addCard(Card.of(2, Suit.HEART));
		hand.updateBet(new BigDecimal("10.0"));
		return IntStream.rangeClosed(0, threadAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			try {
				BlackjackHand hand_ = (BlackjackHand) doubleDownPlayer.getHands().get(0);
				barrier.await();
				hand_.doubleDown(Card.of(3, Suit.CLUB));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				updateRejectedDoubleDowns();
			}
		})).toList();
	}

	private List<Thread> createCasinoPlayers(int playerAmount, CyclicBarrier casinoDoor) {
		return IntStream.rangeClosed(0, playerAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			User indexedUser = new User("player:" + index, table.getId(), UUID.randomUUID(), null, MAX_BET);
			try {
				int seatNumber = index;
				if (index >= table.getSeats().size()) {
					seatNumber = ThreadLocalRandom.current().nextInt(0, table.getSeats().size() - 1);
				}
				casinoDoor.await();
				if (!table.join(indexedUser, String.valueOf(seatNumber))) {
					addRejected();
				} else {
					addAccepted();
					table.bet(indexedUser.userId(), MAX_BET);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		})).toList();
	}

	private List<Thread> addWatchersToTable(int threadAmount, CyclicBarrier casinoDoor) {
		return IntStream.rangeClosed(0, threadAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			User randomUser = new User("watcher:" + index, table.getId(), UUID.randomUUID(), null, MAX_BET);
			try {
				casinoDoor.await();
				table.watch(randomUser);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		})).toList();
	}
}
