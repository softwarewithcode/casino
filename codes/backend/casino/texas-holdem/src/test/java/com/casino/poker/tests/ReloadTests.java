package com.casino.poker.tests;

import com.casino.common.player.PlayerStatus;
import com.casino.common.reload.Reload;
import com.casino.common.reload.ReloadData;
import com.casino.common.reload.Reloadable;
import com.casino.poker.game.HoldemPhase;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ReloadTests extends DefaultTableTests {
    @Test
    public void preFlopReloadIsHandledWhenCurrentRoundIsCompleted() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        Reload reloadButtonPlayer = createReloadData(2, "74.79");
        table.call(table.getActivePlayer().getId()); //UTG
        assertEquals(new BigDecimal("800.00"), table.getPlayer(2).getCurrentBalance());
        table.fold(table.getActivePlayer().getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadButtonPlayer.getInput().reloadable().getId(),reloadButtonPlayer.getInput().reloadId(),reloadButtonPlayer.getInput().reloadAmountAttempt());
        table.fold(table.getActivePlayer().getId());//cutoff
        table.fold(table.getActivePlayer().getId());//button == bridge3 = reloader
        table.fold(table.getActivePlayer().getId());//small blind
        table.check(table.getActivePlayer().getId());// Big blind
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("22.45"));// Big blind raisesTo
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("874.79"), table.getPlayer(2).getCurrentBalance());
        assertEquals(reloadButtonPlayer.getInput().reloadAmountAttempt(), futureReloadResult.get().getUsedAmount());
    }

    private Reload createReloadData(Integer seatNumber, String amount) {
        UUID reloadId = UUID.randomUUID();
        BigDecimal reloadAmountAttempt = new BigDecimal(amount);
        Reloadable player = (Reloadable) table.getPlayer(seatNumber);
        ReloadData data = new ReloadData(reloadId, player, reloadAmountAttempt, table.getDealer().getGameData().maxBuyIn());
        return new Reload(data);
    }

    @Test
    public void reloadingIsDoneUpToLimit() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        Reload reloadButtonPlayer = createReloadData(2, "1200.01");
        table.call(table.getActivePlayer().getId()); //UTG
        assertEquals(new BigDecimal("800.00"), table.getPlayer(2).getCurrentBalance());
        table.fold(table.getActivePlayer().getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadButtonPlayer.getInput().reloadable().getId(),reloadButtonPlayer.getInput().reloadId(),reloadButtonPlayer.getInput().reloadAmountAttempt());
        table.fold(table.getActivePlayer().getId());//cutoff
        table.fold(table.getActivePlayer().getId());//button == bridge3 = reloader
        table.fold(table.getActivePlayer().getId());//small blind
        table.check(table.getActivePlayer().getId());// Big blind
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("22.45"));// Big blind raisesTo
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("1000.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(new BigDecimal("200.00"), futureReloadResult.get().getUsedAmount());
    }

    @Test
    public void reloadingIsDoneUpToLimitEdgeIncluding() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        Reload reloadButtonPlayer = createReloadData(2, "200.00");
        table.call(table.getActivePlayer().getId()); //UTG
        assertEquals(new BigDecimal("800.00"), table.getPlayer(2).getCurrentBalance());
        table.fold(table.getActivePlayer().getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadButtonPlayer.getInput().reloadable().getId(),reloadButtonPlayer.getInput().reloadId(),reloadButtonPlayer.getInput().reloadAmountAttempt());
        table.fold(table.getActivePlayer().getId());//cutoff
        table.fold(table.getActivePlayer().getId());//button == bridge3 = reloader
        table.fold(table.getActivePlayer().getId());//small blind
        table.check(table.getActivePlayer().getId());// Big blind
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("22.45"));// Big blind raisesTo
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("1000.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(new BigDecimal("200.00"), futureReloadResult.get().getUsedAmount());
    }

    @Test
    public void reloadingIsDoneUpToLimitEdgeExcluding() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        Reload reloadButtonPlayer = createReloadData(2, "200.01");
        table.call(table.getActivePlayer().getId()); //UTG
        assertEquals(new BigDecimal("800.00"), table.getPlayer(2).getCurrentBalance());
        table.fold(table.getActivePlayer().getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadButtonPlayer.getInput().reloadable().getId(),reloadButtonPlayer.getInput().reloadId(),reloadButtonPlayer.getInput().reloadAmountAttempt());
        table.fold(table.getActivePlayer().getId());//cutoff
        table.fold(table.getActivePlayer().getId());//button == bridge3 = reloader
        table.fold(table.getActivePlayer().getId());//small blind
        table.check(table.getActivePlayer().getId());// Big blind
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("22.45"));// Big blind raisesTo
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("1000.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(new BigDecimal("200.00"), futureReloadResult.get().getUsedAmount());
    }

    @Test
    public void reloadingNegativeAmountNotPossible() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        assertThrows(IllegalArgumentException.class, () -> createReloadData(2, "-0.00"));
    }

    @Test
    public void reloadingNegativeAmountNotPossible2() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        assertThrows(IllegalArgumentException.class, () -> createReloadData(2, "-0.01"));
    }

    @Test
    public void nothingIsReloadedWhenPlayerBalanceIsMaximumBuyIn() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        Reload reloadUtgSeat5 = createReloadData(5, "200.01");
        assertEquals(new BigDecimal("900.00"), table.getPlayer(5).getCurrentBalance()); //Button
        table.raiseTo(table.getPlayer(5).getId(), new BigDecimal("110.00")); //UTG seat 5
        assertEquals(new BigDecimal("790.00"), table.getPlayer(5).getCurrentBalance()); //Button
        table.fold(table.getActivePlayer().getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadUtgSeat5.getInput().reloadable().getId(),reloadUtgSeat5.getInput().reloadId(),reloadUtgSeat5.getInput().reloadAmountAttempt());
        table.fold(table.getActivePlayer().getId());//cutoff
        table.fold(table.getActivePlayer().getId());//button
        table.fold(table.getActivePlayer().getId());//small blind
        table.call(table.getActivePlayer().getId());// Big blind
        table.check(table.getActivePlayer().getId());// Big blind checks
        table.allIn(table.getActivePlayer().getId()); // UTG allIn
        assertEquals(new BigDecimal("790.00"), table.getPlayer(5).getTableChipCount()); //UTG
        table.fold(table.getActivePlayer().getId()); // BB folds
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("993.75"), table.getPlayer(5).getCurrentBalance()); //Is now BB and then 10.00 goes to bigBlind payment
        assertEquals(new BigDecimal("0.00"), futureReloadResult.get().getUsedAmount());
    }

    @Test
    public void wholeReloadAmountIsReloadedWhenPlayerBalanceIsAlmostZeroAndTimeIsUp() throws ExecutionException, InterruptedException {
        default6PlayersJoin6MaxTable();
        var start=System.currentTimeMillis();
        Reload reloadUtgSeat5 = createReloadData(5, "999.95");
        assertEquals(new BigDecimal("900.00"), table.getPlayer(5).getCurrentBalance()); //Button
        table.raiseTo(table.getPlayer(5).getId(), new BigDecimal("899.99")); //UTG seat 5
        assertEquals(new BigDecimal("0.01"), table.getPlayer(5).getCurrentBalance());
        table.call(table.getPlayer(0).getId());//UTG+1
        CompletableFuture<Reload> futureReloadResult = table.reload(reloadUtgSeat5.getInput().reloadable().getId(),reloadUtgSeat5.getInput().reloadId(),reloadUtgSeat5.getInput().reloadAmountAttempt());
        table.fold(table.getPlayer(1).getId());//cutoff
        table.fold(table.getPlayer(2).getId());//button
        table.fold(table.getPlayer(3).getId());//small blind
        table.fold(table.getPlayer(4).getId());// Big blind
        assertSame(HoldemPhase.FLOP, table.getGamePhase());
        table.check(table.getPlayer(5).getId());// BB
        table.allIn(table.getPlayer(0).getId()); // cutoff allin
        assertEquals(new BigDecimal("0.01"), table.getPlayer(5).getCurrentBalance()); //UTG
        sleep(TestHoldemTableFactory.DEFAULT_PLAYER_TIME, ChronoUnit.SECONDS); 
        assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(5).getStatus());  //User timesout
        waitRoundToStart();
        assertSame(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(new BigDecimal("999.96"), table.getPlayer(5).getCurrentBalance()); // Round started automatically -> 10 for bigBlind
        assertEquals(table.getRound().getBigBlindPlayer(), table.getPlayer(0));
        assertEquals(new BigDecimal("999.95"), futureReloadResult.get().getUsedAmount());
    }
}
