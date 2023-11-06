package com.casino.roulette.tests;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.PlayerStatus;
import com.casino.common.user.User;
import com.casino.roulette.export.RouletteTableAPI;
import com.casino.roulette.game.RouletteGamePhase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RoulettePlayerTest extends RouletteBaseTests {

    @Test
    public void roundDoesNotStartWhenPlayerHasNoBetsInMultiplayerTableAndPlayerStaysActive() {
        assertEquals(1, multiPlayerTable.getDealer().getGameData().getMaxSkips());
        multiPlayerTable.join(usr);
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        var player = multiPlayerTable.getPlayer(usr.userId());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        assertFalse(player.hasTooManySkips());
        assertEquals(1, multiPlayerTable.getWheel().getResultBoard().size());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(1, multiPlayerTable.getWheel().getResultBoard().size());
        assertEquals(1, multiPlayerTable.getActivePlayerCount());
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        assertFalse(player.hasTooManySkips());
    }

    @Test
    public void skippingOneRoundAndContinuingResetsSkipCountWhileOtherPlayerHasBetsSoThatRoundStartsWhenOtherIsSkipping() {
        multiPlayerTable.join(usr);
        multiPlayerTable.join(usr2);
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        var player = multiPlayerTable.getPlayer(usr.userId());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        assertFalse(player.hasTooManySkips());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr2.getId(), 0, TWENTY)); // player2 removed
        assertFalse(player.hasTooManySkips());
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        assertFalse(player.hasTooManySkips());
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        waitForRoundToBeCompleted(multiPlayerTable);
        multiPlayerTable.join(usr2); // Second join
        multiPlayerTable.bet(usr2.getId(), 0, TWENTY);
        assertFalse(player.hasTooManySkips()); // player1 has 1 skip
        assertEquals(PlayerStatus.ACTIVE, player.getStatus());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertTrue(player.hasTooManySkips()); //// player1 has 2 skip
        assertEquals(PlayerStatus.SIT_OUT, player.getStatus());
    }

    @Test
    public void playerWithoutBetsIsRemovedFromTableWhenOtherPlayerPlaysAndNewRoundStarts() {
        multiPlayerTable.join(usr);
        multiPlayerTable.join(usr2);
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        var player = multiPlayerTable.getPlayer(usr.userId());
        waitForRoundToBeCompleted(multiPlayerTable);
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        waitForRoundToBeCompleted(multiPlayerTable);
        multiPlayerTable.join(usr2); // Second join
        multiPlayerTable.bet(usr2.getId(), 0, TWENTY);
        waitForRoundToBeCompleted(multiPlayerTable);
        multiPlayerTable.bet(usr2.getId(), 0, TWENTY);
        waitForRoundToBeCompleted(multiPlayerTable);
        assertTrue(player.hasTooManySkips());
        assertEquals(PlayerStatus.SIT_OUT, player.getStatus());
        assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), 0, TWENTY));
        assertEquals(1, multiPlayerTable.getActivePlayerCount());
    }

    @Test
    public void addingAndRemovingBetsUpdatesTotalPositionsAmountsCalculations() {
        singlePlayerTable.join(usr);
        var player = singlePlayerTable.getPlayer(usr.userId());
        assertEquals(0, player.getPositionsTotalAmounts().size());
        singlePlayerTable.bet(usr.getId(), 17, TWENTY);
        assertEquals(1, player.getPositionsTotalAmounts().size());
        assertEquals(TWENTY, player.getPositionsTotalAmounts().get(17));
        singlePlayerTable.bet(usr.getId(), 17, new BigDecimal("9.97"));
        assertEquals(new BigDecimal("29.97"), player.getPositionsTotalAmounts().get(17));
        assertEquals(1, player.getPositionsTotalAmounts().size());
        singlePlayerTable.removeBets(usr.getId(), false);
        assertEquals(TWENTY, player.getPositionsTotalAmounts().get(17));
        singlePlayerTable.bet(usr.getId(), 111, TWENTY);
        assertEquals(2, player.getPositionsTotalAmounts().size());
        singlePlayerTable.bet(usr.getId(), 111, new BigDecimal("5.45"));
        singlePlayerTable.bet(usr.getId(), 111, new BigDecimal("1.01"));
        singlePlayerTable.bet(usr.getId(), 111, new BigDecimal("1.01"));
        assertEquals(new BigDecimal("27.47"), player.getPositionsTotalAmounts().get(111));
        singlePlayerTable.bet(usr.getId(), 111, new BigDecimal("1.01"));
        singlePlayerTable.bet(usr.getId(), 101, new BigDecimal("6.01"));
        assertEquals(3, player.getPositionsTotalAmounts().size());
        singlePlayerTable.removeBetsFromPosition(usr.getId(), 111);
        assertEquals(2, player.getPositionsTotalAmounts().size());
        assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.removeBetsFromPosition(usr.getId(), 111));
        singlePlayerTable.removeBets(usr.getId(), true);
        assertEquals(0, player.getPositionsTotalAmounts().size());
    }

    @Test
    public void removingAllBetsUpdatesPositionsTotals() {
        singlePlayerTable.join(usr);
        var player = singlePlayerTable.getPlayer(usr.userId());
        assertEquals(0, player.getPositionsTotalAmounts().size());
        singlePlayerTable.bet(usr.getId(), 17, BigDecimal.TEN);
        singlePlayerTable.bet(usr.getId(), 17, BigDecimal.TEN);
        singlePlayerTable.bet(usr.getId(), 17, BigDecimal.TEN);
        singlePlayerTable.removeBets(usr.getId(), true);
        assertEquals(0, player.getPositionsTotalAmounts().size());
    }

    @Test
    public void removingPositionsLastChipLeadsToRemovedPosition() {
        singlePlayerTable.join(usr);
        var player = singlePlayerTable.getPlayer(usr.userId());
        assertEquals(0, player.getPositionsTotalAmounts().size());
        singlePlayerTable.bet(usr.getId(), 17, BigDecimal.TEN);
        singlePlayerTable.removeBets(usr.getId(), false);
        assertEquals(0, player.getPositionsTotalAmounts().size());
    }

    @Test
    public void betsAreReturnedToBalanceWhenPlayerLeavesOnBetPhaseInSinglePlayerTable() {
        singlePlayerTableWithLongSpinningTime.join(usr);
        var player = singlePlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        RouletteTableAPI api = singlePlayerTableWithLongSpinningTime;
        singlePlayerTableWithLongSpinningTime.bet(usr.getId(), 4, TWENTY);
        assertEquals(RouletteGamePhase.BET, singlePlayerTableWithLongSpinningTime.getGamePhase());
        api.leave(usr.getId());
        waitForRoundToBeCompleted(singlePlayerTableWithLongSpinningTime);
        assertEquals(THOUSAND, player.getCurrentBalance());
        assertEquals(0, player.getRoundResults().size());
    }

    @Test
    public void betsStayWhenPlayerLeavesDuringSpinningPhaseAndRoundResultIsUpdatedAccordingly_singlePlayerTable() {
        singlePlayerTableWithLongSpinningTime.join(usr);
        var player = singlePlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        RouletteTableAPI api = singlePlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 0, TWENTY);
        api.play(usr.getId(), singlePlayerTableWithLongSpinningTime.getWheel().getSpinId());
        assertEquals(RouletteGamePhase.SPINNING, singlePlayerTableWithLongSpinningTime.getGamePhase());
        api.leave(player.getId());
        waitForRoundToBeCompleted(singlePlayerTableWithLongSpinningTime);
        assertEquals(new BigDecimal("980.00"), player.getCurrentBalance());
        assertEquals(1, player.getRoundResults().size());
        assertEquals(TWENTY, player.getRoundResults().getLast().playerResult().totalBets());
        assertEquals(BigDecimal.ZERO, player.getRoundResults().getLast().playerResult().totalWinnings());
        assertEquals(TWENTY, player.getRoundResults().getLast().playerResult().losingBets().getLast().getAmount());
    }

    @Test
    public void betsStayWhenPlayerLeavesDuringSpinningPhaseAndRoundResultIsUpdatedAccordingly_singlePlayerTable_2() {
        singlePlayerTableWithLongSpinningTime.join(usr);
        var player = singlePlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        RouletteTableAPI api = singlePlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 4, TWENTY);
        api.play(usr.getId(), singlePlayerTableWithLongSpinningTime.getWheel().getSpinId());
        assertEquals(RouletteGamePhase.SPINNING, singlePlayerTableWithLongSpinningTime.getGamePhase());
        api.leave(player.getId());
        waitForRoundToBeCompleted(singlePlayerTableWithLongSpinningTime);
        assertEquals(new BigDecimal("1700.00"), player.getCurrentBalance());
        assertEquals(1, player.getRoundResults().size());
        assertEquals(TWENTY, player.getRoundResults().getLast().playerResult().totalBets());
        assertEquals(new BigDecimal("700.00"), player.getRoundResults().getLast().playerResult().totalWinnings());
        assertEquals(0, player.getRoundResults().getLast().playerResult().losingBets().size());
    }

    @Test
    public void betsAreReturnedToBalanceWhenPlayerLeavesOnBetPhaseInMultiplayerTable() {
        multiPlayerTable.join(usr);
        var player = multiPlayerTable.getPlayer(usr.getId());
        RouletteTableAPI api = multiPlayerTable;
        multiPlayerTable.bet(usr.getId(), 4, TWENTY);
        assertEquals(new BigDecimal("980.00"), player.getCurrentBalance());
        api.leave(usr.getId());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(THOUSAND, player.getCurrentBalance());
        assertEquals(0, player.getRoundResults().size());
    }

    @Test
    public void betsStayWhenPlayerLeavesDuringSpinningPhaseAndRoundResultIsUpdatedAccordingly_multiplayerTable() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 0, TWENTY);
        assertEquals(RouletteGamePhase.BET, multiPlayerTableWithLongSpinningTime.getGamePhase());
        waitBetTime(multiPlayerTableWithLongSpinningTime);
        assertEquals(RouletteGamePhase.SPINNING, multiPlayerTableWithLongSpinningTime.getGamePhase());
        api.leave(player.getId());
        assertEquals(RouletteGamePhase.SPINNING, multiPlayerTableWithLongSpinningTime.getGamePhase());
        waitForRoundToBeCompleted(multiPlayerTableWithLongSpinningTime);
        assertEquals(new BigDecimal("980.00"), player.getCurrentBalance());
        assertEquals(1, player.getRoundResults().size());
        assertEquals(TWENTY, player.getRoundResults().getLast().playerResult().totalBets());
        assertEquals(BigDecimal.ZERO, player.getRoundResults().getLast().playerResult().totalWinnings());
        assertEquals(TWENTY, player.getRoundResults().getLast().playerResult().losingBets().getLast().getAmount());
    }

    @Test
    public void betIsReturnedWithoutSpinInMultiplayerPlayerTable() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 0, TWENTY);
        api.leave(player.getId());
        waitForRoundToBeCompleted(multiPlayerTableWithLongSpinningTime);
        assertEquals(0, player.getRoundResults().size());
        assertEquals(THOUSAND, player.getCurrentBalance());
        assertEquals(PlayerStatus.LEFT, player.getStatus());
    }

    @Test
    public void seatIsSanitizedWhenPlayerLeavesAfterJoin() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        assertEquals(1, multiPlayerTableWithLongSpinningTime.getActivePlayerCount());
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        api.leave(player.getId());
        assertEquals(0, multiPlayerTableWithLongSpinningTime.getActivePlayerCount());
    }

    @Test
    public void playerStatusAndSeatTestAfterLeave() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        assertEquals(1, multiPlayerTableWithLongSpinningTime.getActivePlayerCount());
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 0, TWENTY);
        api.leave(player.getId());
        waitForRoundToBeCompleted(multiPlayerTableWithLongSpinningTime);
        assertEquals(0, multiPlayerTableWithLongSpinningTime.getActivePlayerCount());
        assertEquals(PlayerStatus.LEFT, player.getStatus());
    }

    @Test
    public void playerStatusesAndSeatsStatesAreModifiedAfterOneLeavesAndOtherPlayerContinues() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        multiPlayerTableWithLongSpinningTime.join(usr2);
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        var player2 = multiPlayerTableWithLongSpinningTime.getPlayer(usr2.getId());
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        api.bet(usr.getId(), 0, TWENTY);
        api.bet(usr2.getId(), 0, TWENTY);
        waitBetTime(multiPlayerTableWithLongSpinningTime);
        api.leave(player.getId());
        waitForRoundToBeCompleted(multiPlayerTableWithLongSpinningTime);
        assertEquals(1, player.getRoundResults().size());
        assertEquals(2, player2.getRoundResults().size());
        assertEquals(PlayerStatus.LEFT, player.getStatus());
        assertEquals(PlayerStatus.ACTIVE, player2.getStatus());
    }

    @Test
    public void removingNonExistingBetsThrowException() {
        singlePlayerTable.join(usr);
        assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.removeBets(usr.getId(), true));
        assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.removeBets(usr.getId(), false));
    }

    @Test
    public void repeatingNonExistingBetsThrowsException() {
        singlePlayerTable.join(usr);
        assertThrows(IllegalPlayerActionException.class, () -> singlePlayerTable.repeatLastBets(usr.getId()));
    }

    @Test
    public void joiningWithInsufficientFundIsNotAllowed() {
        var user = new User("JohnDoe", multiPlayerTable.getId(), UUID.randomUUID(), null, new BigDecimal("0.99"));
        assertThrows(IllegalArgumentException.class, () -> singlePlayerTable.join(user));
    }

    @Test
    public void remainingBetsTotalsAreUpdatedWhenRoundIsCompletedPositionTotalsIsUpdated() {
        multiPlayerTable.join(usr);
        multiPlayerTable.bet(usr.getId(), 200, TWENTY);
        multiPlayerTable.bet(usr.getId(), 201, TWENTY);
        multiPlayerTable.bet(usr.getId(), 202, TWENTY);
        var player = multiPlayerTable.getPlayer(usr.userId());
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(TWENTY, player.getPositionsTotalAmounts().get(200));
        assertEquals(1, player.getPositionsTotalAmounts().size());
    }

    @Test
    public void leavingPlayerPositionsTotalsCorrespondsBetsOnTable() {
        multiPlayerTableWithLongSpinningTime.join(usr);
        RouletteTableAPI api = multiPlayerTableWithLongSpinningTime;
        var player = multiPlayerTableWithLongSpinningTime.getPlayer(usr.getId());
        multiPlayerTableWithLongSpinningTime.bet(usr.getId(), 200, TWENTY);
        multiPlayerTableWithLongSpinningTime.bet(usr.getId(), 201, TWENTY);
        assertEquals(2, player.getPositionsTotalAmounts().size());
        waitBetTime(multiPlayerTableWithLongSpinningTime);
        api.leave(usr.getId());
        waitForRoundToBeCompleted(multiPlayerTableWithLongSpinningTime);
        assertEquals(0, player.getPositionsTotalAmounts().size());
        assertEquals(new BigDecimal("1020.00"), player.getCurrentBalance());
    }

    @Test
    public void previousBetDataIsPreservedIfPlayerDoesNotBet() {
        multiPlayerTable.join(usr);
        multiPlayerTable.join(usr2);
        var player2 = multiPlayerTable.getPlayer(usr2.getId());
        multiPlayerTable.bet(usr.getId(), 0, TWENTY);
        multiPlayerTable.bet(usr2.getId(), 1, TWENTY);
        waitForRoundToBeCompleted(multiPlayerTable);
        multiPlayerTable.bet(usr.getId(), 1, TWENTY);
        waitForRoundToBeCompleted(multiPlayerTable);
        assertEquals(0, player2.getBets().size());
        multiPlayerTable.repeatLastBets(player2.getId());
        assertEquals(1, player2.getBets().get(0).getPosition());
        assertEquals(TWENTY, player2.getBets().get(0).getAmount());
        assertEquals(1, player2.getBets().size());
    } 
}
