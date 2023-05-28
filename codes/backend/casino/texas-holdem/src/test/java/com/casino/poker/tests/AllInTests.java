package com.casino.poker.tests;

import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.user.Bridge;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import com.casino.poker.table.HoldemTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AllInTests extends DefaultTableTests {
    @Test
    public void smallBlindPlayerWinsBigBlindAWithAllInManoeuvrePreFlop() {
        defaultJoinJoin();
        table.allIn(getDefaultTableSmallBlindPlayer().getId());
        table.fold(getDefaultTableBigBlindPlayer().getId());
        assertEquals(new BigDecimal("1010.00"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
    }

    @Test
    public void smallBlindPlayerWinsByGoingAllInAsResponseToRaisePreFlop() {
        defaultJoinJoin();
        table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("100.00"));
        table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("200.00"));
        table.allIn(getDefaultTableSmallBlindPlayer().getId());
        table.fold(getDefaultTableBigBlindPlayer().getId());
        assertEquals(new BigDecimal("1200.00"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("800.00"), getDefaultTableBigBlindPlayer().getCurrentBalance());
    }

    @Test
    public void bbPlayerWinsWithMinorAllInOnTheFlop() {
        defaultJoinJoin();
        table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("100.00"));
        table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("999.99"));
        table.call(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("0.01"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("0.01"), getDefaultTableBigBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("1999.98"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        table.allIn(getDefaultTableBigBlindPlayer().getId());
        table.fold(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("1979.99"), getDefaultTableBigBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("0.01"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
    }

    @Test
    public void tablePotsAreCreatedAfterAllInsOfAllPlayers() {
        // First round starts with the first 2 players
        defaultJoinJoin();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        table.join(bridge3, "4", false); // 800
        table.join(bridge4, "5", false); // 700
        table.join(bridge5, "1", false); // 600
        table.join(bridge6, "0", false); // 900
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        // Second round starts with 6 players
        everybodyMakeAction(table, PokerActionType.ALL_IN);
        assertEquals(5, table.getDealer().getPotHandler().getPots().size());
        assertEquals(new BigDecimal("3580.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount()); // 6*600-20 (cap))
        assertEquals(new BigDecimal("500.00"), table.getDealer().getPotHandler().getPots().get(1).getAmount()); // 5*100 (cap)
        assertEquals(new BigDecimal("400.00"), table.getDealer().getPotHandler().getPots().get(2).getAmount()); // 4*100 (cap)
        assertEquals(new BigDecimal("300.00"), table.getDealer().getPotHandler().getPots().get(3).getAmount()); // 3*100 (cap)
        assertEquals(new BigDecimal("180.00"), table.getDealer().getPotHandler().getPots().get(4).getAmount()); // 2*90-9 (percent + firstRound result)
    }

    @Test
    public void allInsResultsToSplitPot() {
        centPlayersJoinPlayersJoin6MaxTable();
        setDealerNextCardsForStraightFlush();
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().clear());
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        everybodyMakeAction(table, PokerActionType.ALL_IN);
        assertEquals(4, table.getDealer().getPotHandler().getPots().size());
        assertEquals(new BigDecimal("199.93"), table.getPlayer(bridgeWithCents.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("398.85"), table.getPlayer(bridge2WithCents.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("398.85"), table.getPlayer(bridge3WithCents.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("548.11"), table.getPlayer(bridge4WithCents.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("399.11"), table.getPlayer(bridge5WithCents.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("553.11"), table.getPlayer(bridge6WithCents.userId()).getCurrentBalance());

        assertEquals(new BigDecimal("1199.62"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("994.60"), table.getDealer().getPotHandler().getPots().get(1).getAmount());
        assertEquals(new BigDecimal("0.78"), table.getDealer().getPotHandler().getPots().get(2).getAmount());
        assertEquals(new BigDecimal("298.00"), table.getDealer().getPotHandler().getPots().get(3).getAmount());

        assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getRake());
        assertEquals(new BigDecimal("00.00"), table.getDealer().getPotHandler().getPots().get(1).getRake());
        assertEquals(new BigDecimal("00.00"), table.getDealer().getPotHandler().getPots().get(2).getRake());
        assertEquals(new BigDecimal("00.00"), table.getDealer().getPotHandler().getPots().get(3).getRake());
    }

    @Test
    public void allInsResultsToSplitPotWithRakeDeductionFromAllPots() {
        HoldemTable table = TestHoldemTableFactory.createDefaultTexasHoldemCashGameTableWithRakeCap(new BigDecimal("5000"));
        table.join(bridge, "2", false); // 1000
        table.join(bridge2, "3", false); // 1000
        waitRoundToStart();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        Bridge extra = new Bridge("extra", table.getId(), UUID.randomUUID(), null, new BigDecimal("800.19"));
        table.join(extra, "4", false); // 800
        table.join(bridge4, "5", false); // 700
        table.join(bridge5, "1", false); // 600
        table.join(bridge6, "0", false); // 900
        int sbPlayerSeatNumber = table.getRound().getPositions().sbSeatNumber();
        int bbPlayerSeatNumber = table.getRound().getPositions().bbSeatNumber();
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        table.getDealer().getDeck().getCards().add(Card.of(1, Suit.HEART));// River
        table.getDealer().getDeck().getCards().add(Card.of(13, Suit.HEART));// Turn
        table.getDealer().getDeck().getCards().add(Card.of(12, Suit.HEART));// Flop 3
        table.getDealer().getDeck().getCards().add(Card.of(11, Suit.HEART));// Flop 2
        table.getDealer().getDeck().getCards().add(Card.of(10, Suit.HEART));// Flop 1
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().clear());
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        everybodyMakeAction(table, PokerActionType.ALL_IN);
        assertEquals(5, table.getDealer().getPotHandler().getPots().size());
        assertEquals(new BigDecimal("960.49"), table.getPlayer(sbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("940.49"), table.getPlayer(bbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("854.99"), table.getPlayer(bridge6.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("760.18"), table.getPlayer(extra.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("570.00"), table.getPlayer(bridge5.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("665.00"), table.getPlayer(bridge4.userId()).getCurrentBalance());

        assertEquals(new BigDecimal("3420.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("475.00"), table.getDealer().getPotHandler().getPots().get(1).getAmount());
        assertEquals(new BigDecimal("380.72"), table.getDealer().getPotHandler().getPots().get(2).getAmount());
        assertEquals(new BigDecimal("284.45"), table.getDealer().getPotHandler().getPots().get(3).getAmount());
        assertEquals(new BigDecimal("171.00"), table.getDealer().getPotHandler().getPots().get(4).getAmount());

        assertEquals(new BigDecimal("180.00"), table.getDealer().getPotHandler().getPots().get(0).getRake());
        assertEquals(new BigDecimal("25.00"), table.getDealer().getPotHandler().getPots().get(1).getRake());
        assertEquals(new BigDecimal("20.03"), table.getDealer().getPotHandler().getPots().get(2).getRake());
        assertEquals(new BigDecimal("14.97"), table.getDealer().getPotHandler().getPots().get(3).getRake());
        assertEquals(new BigDecimal("9.00"), table.getDealer().getPotHandler().getPots().get(4).getRake());
    }

    @Test
    public void allInsResultsToSplitPotWithRakeCapLimit() {
        HoldemTable table = TestHoldemTableFactory.createDefaultTexasHoldemCashGameTableWithRakeCap(new BigDecimal("227.79"));
        table.join(bridge, "2", false); // 1000
        table.join(bridge2, "3", false); // 1000
        waitRoundToStart();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        Bridge extra = new Bridge("extra", table.getId(), UUID.randomUUID(), null, new BigDecimal("800.19"));
        table.join(extra, "4", false); // 800
        table.join(bridge4, "5", false); // 700
        table.join(bridge5, "1", false); // 600
        table.join(bridge6, "0", false); // 900
        Integer sbPlayerSeatNumber = table.getRound().getPositions().sbSeatNumber();
        Integer bbPlayerSeatNumber = table.getRound().getPositions().bbSeatNumber();
        // Second round starts with 6 players.
        waitRoundToStart();
        table.getDealer().getDeck().getCards().add(Card.of(1, Suit.HEART));// River
        table.getDealer().getDeck().getCards().add(Card.of(13, Suit.HEART));// Turn
        table.getDealer().getDeck().getCards().add(Card.of(12, Suit.HEART));// Flop 3
        table.getDealer().getDeck().getCards().add(Card.of(11, Suit.HEART));// Flop 2
        table.getDealer().getDeck().getCards().add(Card.of(10, Suit.HEART));// Flop 1
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().clear());
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().add(Card.of(2, Suit.SPADE)));
        everybodyMakeAction(table, PokerActionType.ALL_IN);
        assertEquals(5, table.getDealer().getPotHandler().getPots().size());
        assertEquals(new BigDecimal("969.07"), table.getPlayer(sbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("949.07"), table.getPlayer(bbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("859.07"), table.getPlayer(bridge6.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("760.18"), table.getPlayer(extra.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("570.00"), table.getPlayer(bridge5.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("665.00"), table.getPlayer(bridge4.userId()).getCurrentBalance());

        assertEquals(new BigDecimal("3420.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("475.00"), table.getDealer().getPotHandler().getPots().get(1).getAmount());
        assertEquals(new BigDecimal("380.72"), table.getDealer().getPotHandler().getPots().get(2).getAmount());
        assertEquals(new BigDecimal("296.67"), table.getDealer().getPotHandler().getPots().get(3).getAmount());
        assertEquals(new BigDecimal("180.00"), table.getDealer().getPotHandler().getPots().get(4).getAmount());

        assertEquals(new BigDecimal("180.00"), table.getDealer().getPotHandler().getPots().get(0).getRake());
        assertEquals(new BigDecimal("25.00"), table.getDealer().getPotHandler().getPots().get(1).getRake());
        assertEquals(new BigDecimal("20.03"), table.getDealer().getPotHandler().getPots().get(2).getRake());
        assertEquals(new BigDecimal("2.76"), table.getDealer().getPotHandler().getPots().get(3).getRake());
        assertEquals(new BigDecimal("0.00"), table.getDealer().getPotHandler().getPots().get(4).getRake());
    }

    @Test
    public void allInsResultsToDifferentWinnersOfDifferentPots() {
        // First round starts with the first 2 players
        defaultJoinJoin();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "4", false); // 800
        table.join(bridge4, "5", false); // 700
        table.join(bridge5, "1", false); // 600
        table.join(bridge6, "0", false); // 900
        table.fold(table.getRound().getBigBlindPlayer().getId());
        Integer firstRoundSbPlayerSeatNumber = getDefaultTableSmallBlindPlayer().getSeatNumber();
        Integer firstRoundBbPlayerSeatNumber = getDefaultTableBigBlindPlayer().getSeatNumber();
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        // Second round starts with 6 players.
        dealer.getDeck().getCards().add(Card.of(1, Suit.SPADE));// River
        dealer.getDeck().getCards().add(Card.of(13, Suit.HEART));// Turn
        dealer.getDeck().getCards().add(Card.of(12, Suit.HEART));// Flop 3
        dealer.getDeck().getCards().add(Card.of(11, Suit.HEART));// Flop 2
        dealer.getDeck().getCards().add(Card.of(10, Suit.HEART));// Flop 1
        table.getRound().getPlayers().forEach(player -> player.getHoleCards().clear());
        PokerPlayer p0 = table.getPlayer(0); // 900
        PokerPlayer p1 = table.getPlayer(1); // 600
        PokerPlayer p2 = table.getPlayer(2);// 1000
        PokerPlayer p3 = table.getPlayer(3);// 1000
        PokerPlayer p4 = table.getPlayer(4); // 800
        PokerPlayer p5 = table.getPlayer(5); // 700

        p0.getHoleCards().clear();
        p0.getHoleCards().add(Card.of(1, Suit.SPADE));
        p0.getHoleCards().add(Card.of(2, Suit.CLUB));

        p1.getHoleCards().add(Card.of(1, Suit.HEART));
        p1.getHoleCards().add(Card.of(2, Suit.CLUB));

        p2.getHoleCards().clear();
        p2.getHoleCards().add(Card.of(5, Suit.CLUB));
        p2.getHoleCards().add(Card.of(2, Suit.SPADE));

        p3.getHoleCards().clear();
        p3.getHoleCards().add(Card.of(1, Suit.DIAMOND));
        p3.getHoleCards().add(Card.of(2, Suit.CLUB));

        p4.getHoleCards().clear();
        p4.getHoleCards().add(Card.of(9, Suit.HEART));
        p4.getHoleCards().add(Card.of(2, Suit.DIAMOND));

        p5.getHoleCards().clear();
        p5.getHoleCards().add(Card.of(7, Suit.DIAMOND));
        p5.getHoleCards().add(Card.of(10, Suit.DIAMOND));
        everybodyMakeAction(table, PokerActionType.ALL_IN);
        assertEquals(new BigDecimal("100.00"), table.getPlayer(p0.getId()).getCurrentBalance());
        assertEquals(new BigDecimal("3580.00"), table.getPlayer(p1.getId()).getCurrentBalance());
        assertEquals(new BigDecimal("210.00"), table.getPlayer(firstRoundSbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("190.00"), table.getPlayer(firstRoundBbPlayerSeatNumber).getCurrentBalance());
        assertEquals(new BigDecimal("900.00"), table.getPlayer(p4.getId()).getCurrentBalance());
        assertEquals(new BigDecimal("00.00"), table.getPlayer(p5.getId()).getCurrentBalance());
    }

    @Test
    public void allInsInDifferentGamePhasesCreatesSeparatePots() {
        // First round starts with the first 2 players
        defaultJoinJoin();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "4", false); // 800
        table.join(bridge4, "5", false); // 700
        table.join(bridge5, "1", false); // 600
        table.join(bridge6, "0", false); // 900
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        table.check(table.getActivePlayer().getId()); // Has new player's blind on table
        table.check(table.getActivePlayer().getId());// Has new player's blind on table
        table.check(table.getActivePlayer().getId());// Has new player's blind on table
        table.call(table.getActivePlayer().getId()); // Button calls
        table.call(table.getActivePlayer().getId());// sb calls
        assertEquals(4, table.getActivePlayer().getActions().size()); // Raise,fold, allIn, check
        table.check(table.getActivePlayer().getId());
        assertEquals(new BigDecimal("60.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
        onePlayerAllIn_OthersCheckAndCall(HoldemPhase.TURN, "JillDoe");
        assertEquals(new BigDecimal("3600.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
        assertSame(HoldemPhase.TURN, table.getGamePhase());

        onePlayerAllIn_OthersCheckAndCall(HoldemPhase.RIVER, "JackieDoe");
        assertEquals(new BigDecimal("3600.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("500.00"), table.getDealer().getPotHandler().getPots().get(1).getAmount());
        assertSame(HoldemPhase.RIVER, table.getGamePhase());
        assertEquals(2, table.getDealer().getPotHandler().getPots().size());
        setupSplitPot();
        // Last all ins on river
        table.allIn(table.getActivePlayer().getId());
        table.allIn(table.getActivePlayer().getId());
        table.allIn(table.getActivePlayer().getId());
        table.allIn(table.getActivePlayer().getId());
        assertEquals(new BigDecimal("3580.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("500.00"), table.getDealer().getPotHandler().getPots().get(1).getAmount());
        assertEquals(new BigDecimal("400.00"), table.getDealer().getPotHandler().getPots().get(2).getAmount());
        assertEquals(new BigDecimal("300.00"), table.getDealer().getPotHandler().getPots().get(3).getAmount());
        assertEquals(new BigDecimal("180.00"), table.getDealer().getPotHandler().getPots().get(4).getAmount());
        assertEquals(5, table.getDealer().getPotHandler().getPots().size());
        assertSame(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
    }


    @Test
    public void callingShortStackAllInPreFlopAsOnlyPlayerWhoCanActLeadsToRoundCompletedAutomatically() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "4");
        table.join(bridge, "2", false); // 1000
        table.join(bridge3, "4", false); // 800
        waitRoundToStart();
        setDealerNextCardsForStraightFlush();
        table.allIn(table.getPlayer(4).getId());
        table.call(bridge.userId());
        waitRoundStartWithExtraWaitTime(1000);
        assertNotEquals(new BigDecimal("985.00"), table.getPlayer(2));
        assertNotEquals(new BigDecimal("780.00"), table.getPlayer(4));
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(2, table.getRounds().size());
    }

    @Test
    public void autoCompleteWithShowdownThreePlayerGameWhereOneIsAllInPreFlopAndOtherFoldsOnFlop() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "4");
        defaultJoinJoinJoin();
        table.allIn(table.getPlayer(4).getId()); // Smallest stack all in from seat 4
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        table.call(table.getActivePlayer().getId());//seat 2
        table.call(table.getActivePlayer().getId());//seat 3
        assertEquals(HoldemPhase.FLOP, table.getGamePhase());
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("10.00")); //seat3
        table.fold(table.getActivePlayer().getId()); //seat2
        assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
    }

    private void waitRoundStartWithExtraWaitTime(int millisSleep) {
        waitRoundToStart();
        sleep(millisSleep, ChronoUnit.MILLIS);
    }

    private void setDealerNextCardsForStraightFlush() {
        dealer.getDeck().getCards().add(Card.of(1, Suit.HEART));// River
        dealer.getDeck().getCards().add(Card.of(13, Suit.HEART));// Turn
        dealer.getDeck().getCards().add(Card.of(12, Suit.HEART));// Flop 3
        dealer.getDeck().getCards().add(Card.of(11, Suit.HEART));// Flop 2
        dealer.getDeck().getCards().add(Card.of(10, Suit.HEART));// Flop 1
    }


    private void onePlayerAllIn_OthersCheckAndCall(HoldemPhase nextPhase, String whoPutsAllIn) {
        while (table.getGamePhase() != nextPhase) {
            if (table.getActivePlayer().getUserName().equals(whoPutsAllIn))
                table.allIn(table.getActivePlayer().getId());
            else if (HoldemFunctions.hasAction.apply((HoldemPlayer) table.getActivePlayer(), PokerActionType.CHECK))
                table.check(table.getActivePlayer().getId());
            else
                table.call(table.getActivePlayer().getId());
        }
    }
}
