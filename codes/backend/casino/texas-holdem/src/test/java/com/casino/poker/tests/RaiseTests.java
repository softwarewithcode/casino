package com.casino.poker.tests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import com.casino.poker.game.HoldemPhase;
import org.junit.jupiter.api.Test;

import com.casino.common.bet.Range;
import com.casino.common.exception.IllegalBetException;
import com.casino.poker.actions.PokerAction;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.functions.HoldemFunctions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RaiseTests extends DefaultTableTests {

	@Test
	public void preFlopRaiseInIsAnOptionForSmallBlindPlayer() {
		defaultJoinJoin();
		assertTrue(HoldemFunctions.hasAction.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.BET_RAISE));
	}

	@Test
	public void preFlopMaxRaiseIsCurrentBalanceAndChipsOnTable() {
		defaultJoinJoin();
		PokerAction action = HoldemFunctions.getActionType.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.BET_RAISE).get();
		assertEquals(new BigDecimal("1000.00"), action.getRange().max());
	}

	@Test
	public void raisingOverBalanceThrowsException() {
		defaultJoinJoin();
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("1000.01")));
		assertEquals(1, exception.getCode());
	}

	@Test
	public void raisingNegativeAmountThrowsException() {
		defaultJoinJoin();
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("-50.0")));
		assertEquals(1, exception.getCode());
	}

	@Test
	public void creatingNegativeBetRangeThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> new Range(new BigDecimal("-0.01"), BigDecimal.ONE));
	}

	@Test
	public void creatingBetRangeWhereMinimumAmountIsMoreThanMaximumThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> new Range(new BigDecimal("5.0"), new BigDecimal("4.99")));
	}

	@Test
	public void raisingOverBillionThrowsException() {
		maximumJoinJoin();
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), maximumBet.add(new BigDecimal("0.01"))));
		assertEquals(2, exception.getCode());
	}

	@Test
	public void preflopMinRaiseIsTwoTimesBigBlindForTheFirstRaiser() {
		defaultJoinJoin();
		PokerAction action = HoldemFunctions.getActionType.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.BET_RAISE).get();
		assertEquals(new BigDecimal("20.00"), action.getRange().min().setScale(2));
	}

	@Test
	public void raisingLessThanMinimumIsNotAllowed() {
		defaultJoinJoin();
		assertThrows(IllegalBetException.class, () -> table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("4.99")));
	}

	@Test
	public void raisingIncreasesRaiserOwnChipsAtTable() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("77.03"));
		assertEquals(new BigDecimal("77.03"), getDefaultTableSmallBlindPlayer().getTableChipCount());
	}

	@Test
	public void raisingDoesNotIncreaseOtherPlayerMoneyOnTable() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("20.00"));
		assertEquals(new BigDecimal("10.00"), getDefaultTableBigBlindPlayer().getTableChipCount());
	}

	@Test
	public void raisingOpensReRaise() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("21.00"));
		assertTrue(getDefaultTableBigBlindPlayer().hasActionType(PokerActionType.BET_RAISE));
	}

	@Test
	public void minimumReRaiseIsCalculatedFromFirstRaiseAmount() {
		defaultJoinJoinJoin();
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("21.00"));
		PokerAction raiseAction = HoldemFunctions.getActionType.apply(table.getPlayer(3), PokerActionType.BET_RAISE).orElseThrow();
		assertEquals(new BigDecimal("32.00"), raiseAction.getRange().min());
	}

	@Test
	public void reReRaisingIncreasesChipsOnTable() {
		defaultJoinJoin();
		assertEquals(new BigDecimal("15.00"), table.getDealer().countAllPlayersChipsOnTable());
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("24.47"));
		assertEquals(new BigDecimal("34.47"), table.getDealer().countAllPlayersChipsOnTable());
		assertEquals(new BigDecimal("24.47"), getDefaultTableSmallBlindPlayer().getTableChipCount());

		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("38.94"));
		assertEquals(new BigDecimal("63.41"), table.getDealer().countAllPlayersChipsOnTable());
		assertEquals(new BigDecimal("24.47"), getDefaultTableSmallBlindPlayer().getTableChipCount());
		assertEquals(new BigDecimal("38.94"), getDefaultTableBigBlindPlayer().getTableChipCount());
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("53.41"));
		assertEquals(new BigDecimal("53.41"), getDefaultTableSmallBlindPlayer().getTableChipCount());
		assertEquals(new BigDecimal("38.94"), getDefaultTableBigBlindPlayer().getTableChipCount());
		assertEquals(new BigDecimal("92.35"), table.getDealer().countAllPlayersChipsOnTable());
	}

	@Test
	public void raiseUpdatesLastRaiser() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("20.00"));
		assertEquals(getDefaultTableSmallBlindPlayer(), table.getRound().getLastRaiser());
		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("30.00"));
		assertEquals(getDefaultTableBigBlindPlayer(), table.getRound().getLastRaiser());
	}

	@Test
	public void maxRaiseToIsCurrentBalanceAndChipsOnTable() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("27.72"));
		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("45.44"));
		assertEquals(new BigDecimal("1000.00"), HoldemFunctions.getActionType.apply(getDefaultTableSmallBlindPlayer(),PokerActionType.BET_RAISE).get().getRange().max());
	}

	@Test
	public void bigBlindSetsInitialRaiseAmountForCurrentRound() {
		defaultJoinJoin();
		assertNull(table.getRound().getLastRaiser());
		assertEquals(new BigDecimal("5.00"), table.getRound().getInitialRaiseAmount());
	}

	@Test
	public void bbPlayerFoldsDirectlyOnTheFlopAndLoses() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("100.00"));
		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("200.00"));
		table.call(getDefaultTableSmallBlindPlayer().getId());
		table.fold(getDefaultTableBigBlindPlayer().getId());
		assertEquals(new BigDecimal("1180.00"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
		assertEquals(new BigDecimal("800.00"), getDefaultTableBigBlindPlayer().getCurrentBalance());
	}

	@Test
	public void raisingUnderMinimumThrowsException2() {
		// First round starts with the first 2 players
		table.join(user, "2", false); // 1000
		table.join(user2, "3", false); // 1000
		waitRoundToStart();
		table.allIn(table.getRound().getSmallBlindPlayer().getId());
		table.fold(table.getRound().getBigBlindPlayer().getId());
		table.join(user3, "4", false); // 800
		table.join(user4, "5", false); // 700
		table.join(user5, "1", false); // 600
		table.join(user6, "0", false); // 900
		sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
		// Second round starts with 6 players.

		assignIrrevelantCardsForPlayers(table);
		assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
		table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("41.27")); // UTG
		table.call(table.getActivePlayer().getId()); // UTGPlus1
		table.call(table.getActivePlayer().getId()); // CUTOFF
		table.call(table.getActivePlayer().getId());// BUTTON
		table.call(table.getActivePlayer().getId());// SB
		table.fold(table.getActivePlayer().getId());// BB
		assertSame(table.getGamePhase(), HoldemPhase.FLOP);
		assertEquals(new BigDecimal("216.35"), table.getDealer().getPotHandler().getActivePotAmount());
		assertEquals(1, table.getDealer().getPotHandler().getPots().size());
		table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("10.01"));// SB
		table.call(table.getActivePlayer().getId());// UTG
		table.call(table.getActivePlayer().getId());// UTGPlus1
		table.call(table.getActivePlayer().getId());// CUTOFF
		table.call(table.getActivePlayer().getId());// BUTTON
		assertSame(table.getGamePhase(), HoldemPhase.TURN);
		assertEquals(new BigDecimal("266.40"), table.getDealer().getPotHandler().getActivePotAmount());
		assertEquals(1, table.getDealer().getPotHandler().getPots().size());
		table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("44.99"));
		assertThrows(IllegalBetException.class, () -> table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("89.97")));
		assertThrows(IllegalBetException.class, () -> table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("648.73")));
		table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("648.72"));
	}

	@Test
	public void raiseIsAvailableBalanceJustCoversMinimumReRaise() {
		defaultJoinJoinJoin();
		// Seat2= 1000, seat3=1000, seat4=800, has bigBlind of 10 at the table
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("399.999").setScale(2, RoundingMode.DOWN));
		table.fold(table.getPlayer(3).getId());
		PokerAction raiseAction = HoldemFunctions.getActionType.apply(table.getPlayer(4), PokerActionType.BET_RAISE).orElseThrow();
		assertEquals(new BigDecimal("789.98"), raiseAction.getRange().min());
	}

	@Test
	public void raiseIsNotAvailableWhenBalanceDoesNotCoverMinimumReRaise() {
		defaultJoinJoinJoin();
		// Seat2= 1000, seat3=1000, seat4=800, has bigBlind of 10 at the table
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("400.00").setScale(2, RoundingMode.DOWN));
		table.fold(table.getPlayer(3).getId());
		assertThrows(NoSuchElementException.class, () -> HoldemFunctions.getActionType.apply(table.getPlayer(4), PokerActionType.BET_RAISE).orElseThrow());
	}
	
	@Test
	public void allInIsAvailableWhenBalanceDoesNotCoverMinimumReRaise() {
		defaultJoinJoinJoin();
		// Seat2= 1000, seat3=1000, seat4=800, has bigBlind of 10 at the table
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("400.00").setScale(2, RoundingMode.DOWN));
		table.fold(table.getPlayer(3).getId());
		PokerAction raiseAction = HoldemFunctions.getActionType.apply(table.getPlayer(4), PokerActionType.ALL_IN).orElseThrow();
		assertEquals(new BigDecimal("790.00"), raiseAction.getRange().min());
	}
	
	@Test
	public void allInIsAvailableWhenBalanceDoesNotCoverMinimumReRaise_2() {
		defaultJoinJoinJoin();
		// Seat2= 1000, seat3=1000, seat4=800, has bigBlind of 10 at the table
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("1000.00").setScale(2, RoundingMode.DOWN));
		table.fold(table.getPlayer(3).getId());
		PokerAction raiseAction = HoldemFunctions.getActionType.apply(table.getPlayer(4), PokerActionType.ALL_IN).orElseThrow();
		assertEquals(new BigDecimal("790.00"), raiseAction.getRange().min());
	}
}
