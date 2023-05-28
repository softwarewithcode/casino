package com.casino.poker.tests;

import com.casino.common.player.PlayerStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class MissedBlindBetTests extends DefaultTableTests {

    @Test
    public void playerMissingBigBlindIncreasesMissedBlindsCount() {
        fourJoinsUTGSitsOutAndEverybodyFolds();
        waitRoundToStart();
        assertTrue(table.getDealer().getBlindsHandler().isMissingBigBlind(table.getPlayer(5)));
    }

    @Test
    public void missedBlindBetCountIsIncreasedByTwoForThePlayerWhoMissesBothBlinds() {
        fourJoinsUTGSitsOutAndEverybodyFolds();
        waitRoundToStart();
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        assertTrue(table.getDealer().getBlindsHandler().isMissingBothBlinds(table.getPlayer(5)));
    }

    @Test
    public void payingBothMissedBlindBetsIncreasesPotBySmallBlindAmountAndPlayersChipsOnTableByBigBlindAmount() {
        fourJoinsUTGSitsOutAndEverybodyFolds();
        waitRoundToStart();
        //Round2
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        //Round3
        table.continueGame(table.getPlayer(5).getId());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        assertTrue(table.getDealer().getBlindsHandler().isMissingBigBlind(table.getPlayer(5)));
        waitRoundToStart();
        //Round4
        assertEquals(new BigDecimal("30.00"),  table.getDealer().getPotHandler().getActivePotAmountWithTableChips());
        assertFalse(table.getDealer().getBlindsHandler().isMissingBigBlind(table.getPlayer(5)));
        assertEquals(table.getPlayer(4), table.getRound().getBigBlindPlayer());
        assertNotEquals(table.getPlayer(5), table.getRound().getBigBlindPlayer());
        assertEquals(new BigDecimal("10.00"), table.getPlayer(5).getTableChipCount());
    }

    @Test
    public void continuingGameImmediatelyAfterMissedBigBlindLeadsToMissingSmallBlindAlso() {
        fourJoinsUTGSitsOutAndEverybodyFolds();
        waitRoundToStart();
        //Round2, button =3, sb=4, 5=sitOut, bb=2
        assertEquals(table.getPlayer(2), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(4), table.getRound().getSmallBlindPlayer());
        assertEquals(3, table.getRound().getPositions().buttonSeatNumber());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        table.continueGame(table.getPlayer(5).getId());
        assertTrue(table.getDealer().getBlindsHandler().isMissingBigBlind(table.getPlayer(5)));
        waitRoundToStart();
        //Round3, button=4, 5=ActiveButCannotPlayInBetweenButtonAndBB, sb=2, bb=3
        assertEquals(4, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(table.getPlayer(2), table.getRound().getSmallBlindPlayer());
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertEquals(3, table.getRound().getPlayers().size());
        assertTrue(table.getDealer().getBlindsHandler().isMissingBothBlinds(table.getPlayer(5)));
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        //Round4 button = 2, sb=3, bb=4, 5=continue +missedBlinds
        assertFalse(table.getDealer().getBlindsHandler().isMissingAnyBlind(table.getPlayer(5)));
        assertEquals(table.getPlayer(4), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(3), table.getRound().getSmallBlindPlayer());
        assertEquals(2, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(new BigDecimal("30.00"), table.getDealer().getPotHandler().getActivePotAmountWithTableChips());
        assertEquals(new BigDecimal("10.00"), table.getPlayer(5).getTableChipCount());
    }

    @Test
    public void continuingGameAfterMissingOnlySmallBlindLeadsToPotSizeIncreaseBySmallBlindAmount() {
        defaultJoinJoinJoinJoin();
        //Round1, 2=button, 3=sb, 4=bb, 5=utg
        table.fold(table.getActivePlayer().getId());
        table.sitOutNextHand(table.getPlayer(4).getId());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        waitRoundToStart();
        //Round2, 2=utg, 3=button, 4=sitOut, 5=bb , sb=nobody
        assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(4).getStatus());
        assertTrue(table.getDealer().getBlindsHandler().isMissingSmallBlind(table.getPlayer(4)));
        assertEquals(table.getPlayer(5), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().buttonPlayer());
        assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(4).getStatus());
        table.continueGame(table.getPlayer(4).getId());
        assertEquals(3, table.getActivePlayerCount());
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal(("15.00")));
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertEquals(3, table.getRound().getPositions().buttonSeatNumber());
        waitRoundToStart();
    }


    private void fourJoinsUTGSitsOutAndEverybodyFolds() {
        defaultJoinJoinJoinJoin();
        table.sitOutNextHand(table.getPlayer(5).getId());
        table.fold(table.getPlayer(5).getId());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
    }
}
