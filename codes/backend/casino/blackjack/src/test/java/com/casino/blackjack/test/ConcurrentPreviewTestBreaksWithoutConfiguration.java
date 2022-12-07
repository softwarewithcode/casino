package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * -> Add VM Arguments -> "-ea --enable-preview" <br>
 * With other IDEs something similar.
 */
public class ConcurrentPreviewTestBreaksWithoutConfiguration extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			rejectedCount = 0;
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

	private Integer rejectedCount = 0;

	@Test
	public void threeOutOfTenConcurrentPlayersDontGetSeatInASevenSeatedTable() throws InterruptedException, BrokenBarrierException {
		CyclicBarrier casinoBarrier = new CyclicBarrier(11);
		List<Thread> threads = createCasinoPlayers(10, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		System.out.println("casino opens. Conductor is:" + Thread.currentThread());
		sleep(2, ChronoUnit.SECONDS);// MainThread waits 2 seconds for VirtualThreads to finish
		assertEquals(3, rejectedCount);
	}

	@Test
	public void fiftyOneOutHundredPlayersGetsRejectedAnd49GetsAcceptedIn49SeatedTable() throws InterruptedException, BrokenBarrierException {
		table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY, MIN_PLAYERS, 49, 49, Type.PUBLIC), UUID.randomUUID());
		CyclicBarrier casinoBarrier = new CyclicBarrier(101);
		List<Thread> threads = createCasinoPlayers(100, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		System.out.println("casino opens. Conductor is:" + Thread.currentThread());
		sleep(3, ChronoUnit.SECONDS);
		assertEquals(49, table.getReservedSeatCount());
		assertEquals(51, rejectedCount);
	}

	private List<Thread> createCasinoPlayers(int amount, CyclicBarrier casinoDoor) {
		return IntStream.rangeClosed(0, amount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			BlackjackPlayer b = new BlackjackPlayer("player:" + index, UUID.randomUUID(), MAX_BET, table);
			try {
				int seatNumber = index;
				if (index >= table.getSeats().size()) {
					seatNumber = ThreadLocalRandom.current().nextInt(0, table.getSeats().size() - 1);
					System.out.println("random seat number to try:" + seatNumber);
				}
				System.out.println(b.getName() + " waits for casino to open:");
				casinoDoor.await();
				if (table.trySeat(seatNumber, b))
					System.out.println(b.getName() + " got seat    " + seatNumber + " ");
				else {
					System.out.println(b.getName() + " failed seat " + seatNumber);
					rejectedCount++;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		})).toList();
	}
}
