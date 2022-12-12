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

import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

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
public class ConcurrentPreviewTestBreaksWithoutConfiguration extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	BlackjackPlayer doubleDownPlayer;
	private BlackjackDealer dealer;
	private volatile int playersWhoGotSeat;
	private volatile int playerWhoDidNotGetSeat;
	private volatile int rejectedInsurances;
	private volatile int rejectedDoubles;
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ConcurrentPreviewTestBreaksWithoutConfiguration.class.getName());

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			doubleDownPlayer = new BlackjackPlayer("player:" + 0, UUID.randomUUID(), new BigDecimal("10000000.0"), table);
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
	public void fiftyOneOutHundredPlayersGetsRejectedAnd49GetsAcceptedIn49SeatedTable() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 49, 49, Type.PUBLIC), UUID.randomUUID());
		CyclicBarrier casinoBarrier = new CyclicBarrier(101);
		List<Thread> threads = createCasinoPlayers(100, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(3, ChronoUnit.SECONDS);
		assertEquals(49, table.getReservedSeatCount());
		assertEquals(49, playersWhoGotSeat);
		assertEquals(51, playerWhoDidNotGetSeat);
	}

	@Test
	public void insuranceCanBetSetOnlyOnceByPlayer() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 49, 49, Type.PUBLIC), UUID.randomUUID());
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
		List<Thread> threads = sendSimultaneouslyMultipleInsuranceRequestsFromSelectedPlayer(28, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(BET_ROUND_TIME_SECONDS + INSURANCE_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		assertEquals(27, rejectedInsurances);
	}

	@Test
	public void doubleDownCanDoneOnlyOnceDirectlyCallingHand() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 49, 49, Type.PUBLIC), UUID.randomUUID());
		System.out.println(doubleDownPlayer.getBalance());
		CyclicBarrier casinoBarrier = new CyclicBarrier(1001);
		List<Thread> threads = createDoubleDownThreads(1000, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		sleep(1, ChronoUnit.SECONDS);
		assertEquals(999, rejectedDoubles);
	}

	@Test
	public void doubleDownCanBeDoneOnlyOnceByPlayer() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 49, 49, Type.PUBLIC), UUID.randomUUID());
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
		sleep(BET_ROUND_TIME_SECONDS + waitSecondsForPlayersToFinish, ChronoUnit.SECONDS);
		BlackjackPlayer b = (BlackjackPlayer) table.getPlayer(0);
		assertEquals(15, b.getFirstHandFinalValue());
		assertEquals(MAX_BET.multiply(BigDecimal.TWO).setScale(2), table.getPlayer(0).getTotalBet());
		assertEquals(MAX_BET.multiply(BigDecimal.TWO).setScale(2), b.getBet(0));
		assertTrue(b.hasDoubled());
		assertEquals(27, rejectedDoubles);
	}

	private List<Thread> sendSimultaneouslyMultipleDoubleDownsFromPlayers(int playerAmount, CyclicBarrier casinoDoor) {
		UUID uuid = UUID.randomUUID();
		BlackjackPlayer doubler1 = new BlackjackPlayer("player:" + 0, uuid, new BigDecimal("10000000.0"), table);
		return IntStream.rangeClosed(0, playerAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			BlackjackPlayer b = null;
			if (index > 0)
				b = new BlackjackPlayer("player:" + index, UUID.randomUUID(), new BigDecimal("10000000.0"), table);
			try {
				int seatNumber = index;
				casinoDoor.await();
				if (index == 0) {
					table.join(0, doubler1);
					table.bet(doubler1, MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS + 2, ChronoUnit.SECONDS);
					table.doubleDown(doubler1);
				} else {
					table.join(seatNumber, b);
					table.bet(b, MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS + 2, ChronoUnit.SECONDS);
					table.doubleDown(doubler1);//// All these players try to doubleDown for the first player
				}

			} catch (Exception e) {
				LOGGER.log(Level.INFO, "doubling down excption:", e);
				updateRejectedDoubleDowns();
			}
		})).toList();
	}

	private List<Thread> sendSimultaneouslyMultipleInsuranceRequestsFromSelectedPlayer(int amount, CyclicBarrier casinoDoor) {
		UUID uuid = UUID.randomUUID();
		BlackjackPlayer insurerRef = new BlackjackPlayer("player:" + 0, uuid, new BigDecimal("10000000.0"), table);
		return IntStream.rangeClosed(0, amount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			BlackjackPlayer b = null;
			if (index > 0)
				b = new BlackjackPlayer("player:" + index, UUID.randomUUID(), new BigDecimal("10000000.0"), table);
			try {
				int seatNumber = index;
				casinoDoor.await();
				if (index == 0) {
					table.join(seatNumber, insurerRef);
					table.bet(insurerRef, MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
					table.insure(insurerRef);
				} else {
					table.join(seatNumber, b);
					table.bet(b, MAX_BET);
					sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
					table.insure(insurerRef);
				}

			} catch (Exception e) {
				LOGGER.log(Level.INFO, "insurance excption:", e);
				updateRejectedInsuranceCount();
			}
		})).toList();
	}

	private List<Thread> createDoubleDownThreads(int amount, CyclicBarrier casinoDoor) {
		BlackjackHand hand = (BlackjackHand) doubleDownPlayer.getActiveHand();
		hand.addCard(Card.of(2, Suit.CLUB));
		hand.addCard(Card.of(2, Suit.HEART));
		hand.updateBet(new BigDecimal("10.0"));
		return IntStream.rangeClosed(0, amount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			try {
				BlackjackHand hand_ = (BlackjackHand) doubleDownPlayer.getHands().get(0);
				casinoDoor.await();
				hand_.doubleDown(Card.of(3, Suit.CLUB));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				updateRejectedDoubleDowns();
			}
		})).toList();
	}

	private List<Thread> createCasinoPlayers(int amount, CyclicBarrier casinoDoor) {
		return IntStream.rangeClosed(0, amount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			BlackjackPlayer b = new BlackjackPlayer("player:" + index, UUID.randomUUID(), MAX_BET, table);
			try {
				int seatNumber = index;
				if (index >= table.getSeats().size()) {
					seatNumber = ThreadLocalRandom.current().nextInt(0, table.getSeats().size() - 1);
				}
				casinoDoor.await();
				if (!table.join(seatNumber, b)) {
//					System.out.println(b.getName() + " did not get seat " + seatNumber + " sees rejectedCount _before updating " + rejectedCount + " 	** " + System.nanoTime());
					addRejected();
//					System.out.println(b.getName() + " did not get seat " + seatNumber + " sees rejectedCounter _after update " + rejectedCount + " 	** " + System.nanoTime());
				} else {
//					System.out.println(b.getName() + " got seat " + seatNumber + " sees acceptedCount before_ updating " + acceptedCount + " 	** " + System.nanoTime());
					addAccepted();
//					System.out.println(b.getName() + " got seat " + seatNumber + "  sees acceptedCount after_ update " + acceptedCount + " ** " + System.nanoTime());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		})).toList();
	}
}
