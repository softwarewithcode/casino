package com.casino.roulette.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.casino.roulette.game.RouletteGamePhase;

public class ParallelBetsTests extends RouletteBaseTests {

	private synchronized void addRejectedBet() {
		rejected++;
	}

	@Test
	public void parallelBetsAreCounted() throws InterruptedException, BrokenBarrierException {
		CyclicBarrier casinoBarrier = new CyclicBarrier(101);
		multiPlayerTable.join(usr2);
		List<Thread> threads = createSingleNumberBets(100, casinoBarrier);
		threads.forEach(Thread::start);
		casinoBarrier.await();
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(51, rejected);
		assertEquals(new BigDecimal("45099.51"), multiPlayerTable.getPlayer(usr2.getId()).getCurrentBalance());
	}

	@Test
	public void playFunctionalityCanBeCalledOncePerSpin() throws InterruptedException, BrokenBarrierException {
		CyclicBarrier casinoBarrier = new CyclicBarrier(1001);
		singlePlayerTable.join(usr);
		singlePlayerTable.bet(usr.getId(), 36, TWENTY);
		assertEquals(TWENTY, singlePlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(new BigDecimal("980.00"), singlePlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(RouletteGamePhase.BET, singlePlayerTable.getGamePhase());
		List<Thread> threads = createPlayFunctionThreadsForSinglePlayerTable(1000, casinoBarrier, singlePlayerTable.getWheel().getSpinId());
		threads.forEach(Thread::start);
		casinoBarrier.await();
		waitForRoundToBeCompleted(singlePlayerTable);
		assertEquals(1, singlePlayerTable.getWheel().getResultBoard().size());
		assertEquals(4, singlePlayerTable.getWheel().getResultBoard().get(0).winningNumber());
		assertEquals(999, rejected);
	}

	private List<Thread> createSingleNumberBets(int threadAmount, CyclicBarrier barrier) {
		return IntStream.rangeClosed(0, threadAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			try {
				barrier.await();
				multiPlayerTable.bet(usr2.getId(), 0, new BigDecimal("100.01"));
			} catch (Exception e) {
				addRejectedBet();
			}
		})).toList();
	}

	private List<Thread> createPlayFunctionThreadsForSinglePlayerTable(int threadAmount, CyclicBarrier barrier, UUID spinId) {
		return IntStream.rangeClosed(0, threadAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
			try {
				barrier.await();
				singlePlayerTable.play(usr.getId(), spinId);
			} catch (Exception e) {
				addRejectedBet();
			}
		})).toList();
	}
}
