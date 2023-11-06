package com.casino.roulette.tests;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.roulette.export.BetType;
import com.casino.roulette.game.RouletteGamePhase;
import com.casino.roulette.player.RoulettePlayer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SingleNumberTests extends RouletteBaseTests {

	@Test
	public void singleNumberBetIsAddedToPlayer() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 0, TWENTY);
		RoulettePlayer p = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(1, p.getBets().size());
	}

	@Test
	public void singleNumberBetThrowsExceptionDueToInsufficientFunds() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), 0, new BigDecimal("1000.01")));
		RoulettePlayer p = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(0, p.getBets().size());
	}

	@Test
	public void balanceIsUpdatedAfterSingleNumberBet() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 0, TWENTY);
		RoulettePlayer p = multiPlayerTable.getPlayer(usr.userId());
		assertEquals(new BigDecimal("980.00"), p.getCurrentBalance());
	}

	@Test
	public void betOnLowerNumberThanWhatIsOnBoardThrowsException() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), -1, TWENTY));
	}

	@Test
	public void betOnHigherNumberThanWhatIsOnBoardThrowsException() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), 209, TWENTY));
	}

	@Test
	public void betOnNothingThrowsException() {
		multiPlayerTable.join(usr);
		assertThrows(IllegalArgumentException.class, () -> multiPlayerTable.bet(usr.getId(), null, TWENTY));
	}

	@Test
	public void singleNumberBetTypeIsAssigned() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 19, TWENTY);
		assertEquals(BetType.SINGLE_NUMBER, multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getType());
	}

	@Test
	public void singleNumberBetScaleIsAssigned() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 19, TWENTY);
		assertEquals(35, multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getType().getPaysOut());
	}

	@Test
	public void betIsAllowedOnlyInBetPhase() {
		multiPlayerTable.join(usr);
		assertEquals(RouletteGamePhase.BET, multiPlayerTable.getGamePhase());
		multiPlayerTable.bet(usr.getId(), 36, TWENTY);
		List<RouletteGamePhase> phases = Stream.of(RouletteGamePhase.values()).filter(phase -> phase != RouletteGamePhase.BET).toList();
		assertEquals(4, phases.size());
		phases.forEach(phase -> {
			multiPlayerTable.updateGamePhase(phase);
			assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.bet(usr.getId(), 31, TWENTY));
		});
	}

	@Test
	public void multipleSingleNumberBetsAddUp() {
		multiPlayerTable.join(usr);
		assertEquals(RouletteGamePhase.BET, multiPlayerTable.getGamePhase());
		multiPlayerTable.bet(usr.getId(), 36, TWENTY);
		multiPlayerTable.bet(usr.getId(), 31, TWENTY);
		assertEquals(2, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		assertEquals(new BigDecimal("960.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
	}

	@Test
	public void singleNumberBetPaysOut35To1() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 4, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1680.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("1700.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(TWENTY, multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getAmount());
	}

	@Test
	public void wrongSingleNumberBetDoesNotPayOut() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 2, TWENTY);
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("980.00"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("980.00"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

	@Test
	public void winningSingleNumberBetStaysAndLosingBetIsRemoved() {
		multiPlayerTable.join(usr);
		multiPlayerTable.bet(usr.getId(), 4, new BigDecimal("11.11"));
		multiPlayerTable.bet(usr.getId(), 36, new BigDecimal("10.00"));
		assertEquals(new BigDecimal("978.89"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		waitBetTime(multiPlayerTable);
		assertEquals(new BigDecimal("1367.74"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("1378.85"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("11.11"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		waitForRoundToBeCompleted(multiPlayerTable);
		assertEquals(1, multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		assertEquals(new BigDecimal("1756.59"), multiPlayerTable.getPlayer(usr.getId()).getCurrentBalance());
		assertEquals(new BigDecimal("1767.70"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
		assertEquals(new BigDecimal("11.11"), multiPlayerTable.getPlayer(usr.getId()).getTotalBet());
		assertEquals(BetType.SINGLE_NUMBER, multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getType());
		assertEquals(4,multiPlayerTable.getPlayer(usr.getId()).getBets().get(0).getPosition());
		assertEquals(1,multiPlayerTable.getPlayer(usr.getId()).getBets().size());
		assertEquals(new BigDecimal("1767.70"), multiPlayerTable.getPlayer(usr.getId()).getTotalOnTable());
	}

}
