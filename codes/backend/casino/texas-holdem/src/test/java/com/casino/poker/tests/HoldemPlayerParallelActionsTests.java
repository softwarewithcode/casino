package com.casino.poker.tests;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.PlayerStatus;
import com.casino.common.user.User;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.table.HoldemTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * * -> Use VM Arguments -> "-ea --enable-preview" <br>
 */
public class HoldemPlayerParallelActionsTests extends DefaultTableTests {

    private volatile int rejectedRaises = 0;

    private synchronized void updateRejectedRaises() {
        rejectedRaises++;
    }

    @Test
    public void onlyOneRaisePerTurnIsAllowed() throws InterruptedException, BrokenBarrierException {
        CyclicBarrier raiseBarrier = new CyclicBarrier(10001);
        List<Thread> raisingThreads = createRaisingThreads(10000, raiseBarrier);
        defaultJoinJoin();
        raisingThreads.forEach(Thread::start);
        raiseBarrier.await();
        sleep(6, ChronoUnit.SECONDS);
        assertEquals(9999, rejectedRaises);
    }

    @Test
    public void sitoutTests() throws InterruptedException, BrokenBarrierException {
        CyclicBarrier sitOutBarrier = new CyclicBarrier(1001);
        HoldemTable bigTable = TestHoldemTableFactory.createTableWithSeatCount(1000);
        List<Thread> sitoutThreads = createSitOutThreads(bigTable, sitOutBarrier);
        sitoutThreads.forEach(Thread::start);
        sitOutBarrier.await();
        sleep(3, ChronoUnit.SECONDS);
        assertEquals(999, bigTable.getPlayers().stream().filter(player -> player.getStatus() == PlayerStatus.SIT_OUT_AS_NEW).count());
    }

    private List<Thread> createSitOutThreads(HoldemTable table, CyclicBarrier casinoBarrier) {
        return IntStream.rangeClosed(0, table.getSeats().size() - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
            User indexedUser = new User("player:" + index, table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
            try {
                table.join(indexedUser, String.valueOf(index), false);
                casinoBarrier.await();
                if (index != 456) // One "random=456" player does not sit out
                    table.sitOutNextHand(indexedUser.userId());
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        })).toList();
    }

    private List<Thread> createRaisingThreads(int threadAmount, CyclicBarrier barrier) {
        return IntStream.rangeClosed(0, threadAmount - 1).mapToObj(index -> Thread.ofVirtual().unstarted(() ->
        {
            try {
                PokerPlayer player = getDefaultTableSmallBlindPlayer();
                barrier.await();
                table.raiseTo(player.getId(), new BigDecimal("20.00"));
            } catch (IllegalPlayerActionException e) {
                updateRejectedRaises();
            } catch (Exception e) {
                System.err.println("NOT EXPECTED, ERROR IN TEST ->" + e);
            }
        })).toList();
    }
}
