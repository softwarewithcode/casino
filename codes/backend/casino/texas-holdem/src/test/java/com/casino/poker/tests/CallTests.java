package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import com.casino.poker.actions.PokerActionType;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.HoldemPhase;

public class CallTests extends DefaultTableTests {
	@Test
	public void callIncreasesSbPlayerMoneyOnTable() {
		defaultJoinJoinCall();
		assertEquals(new BigDecimal("10.00"), table.getRound().getSmallBlindPlayer().getTableChipCount());
	}

	@Test
	public void callIncreasesTotalMoneyOnTable() {
		defaultJoinJoinCall();
		assertEquals(new BigDecimal("20.00"), table.getDealer().countAllPlayersChipsOnTable());
	}

	@Test
	public void callDoesNotUpdateLastPlayerToSpeak() {
		defaultJoinJoinCall();
		assertEquals(table.getRound().getBigBlindPlayer(), table.getRound().getLastSpeakingPlayer());
	}

	@Test
	public void callClearsAvailableActions() {
		defaultJoinJoinCall();
		assertEquals(0, table.getRound().getSmallBlindPlayer().getActions().size());
	}

	@Test
	public void callChangesActivePlayer() {
		defaultJoinJoinCall();
		assertEquals(table.getRound().getBigBlindPlayer(), table.getActivePlayer());
	}

	@Test
	public void callingReRaiseCreatesMainPot() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("23.45"));
		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("47.94"));
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("99.41"));
		table.call(getDefaultTableBigBlindPlayer().getId());
		assertEquals(new BigDecimal("198.82"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
	}

	@Test
	public void chipsAreClearedFromTableWhenHoldemPhaseChanges() {
		defaultJoinJoin();
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("23.45"));
		table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("47.94"));
		table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("99.41"));
		table.call(getDefaultTableBigBlindPlayer().getId());
		assertEquals(new BigDecimal("0.00"), getDefaultTableSmallBlindPlayer().getTableChipCount());
		assertEquals(new BigDecimal("0.00"), getDefaultTableBigBlindPlayer().getTableChipCount());
	}

	@Test
	public void callCheckChangesStateFromPreFlopToFlop() {
		defaultJoinJoin();
		table.call(getDefaultTableSmallBlindPlayer().getId());
		table.check(getDefaultTableBigBlindPlayer().getId());
		assertEquals(HoldemPhase.FLOP, table.getGamePhase());
	}

	@Test
	public void callCheckCreatesMainPot() {
		defaultJoinJoin();
		table.call(getDefaultTableSmallBlindPlayer().getId());
		table.check(getDefaultTableBigBlindPlayer().getId());
		assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
	}

	@Test
	public void callIsNotAvailableWhenBalanceDoesNotCoverCalling() {
		defaultJoinJoinJoin();
		// Seat2= 1000, seat3=1000, seat4=800, has bigBlind of 10 at the table
		table.raiseTo(table.getPlayer(2).getId(), new BigDecimal("1000.00").setScale(2, RoundingMode.DOWN));
		table.fold(table.getPlayer(3).getId());
		assertThrows(NoSuchElementException.class, () -> HoldemFunctions.getActionType.apply(table.getPlayer(4), PokerActionType.CALL).orElseThrow());
	}
}
