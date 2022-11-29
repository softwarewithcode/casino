package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.table.Phase;

public class DealerTest extends BaseTest {
	@Test
	public void betPhaseStartsAndEnds() {
		var dealer = (BlackjackDealer) publicTable.getDealer();
		assertNull(dealer.getTable().getPhase());
		takeSeat(1, blackjackPlayer);
		assertEquals(Phase.BET, dealer.getTable().getPhase());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(Phase.INITIAL_DEAL_COMPLETED, dealer.getTable().getPhase());
	}

	@Test
	public void playerReceivesTwoCardsInFirstHandDuringInitialDeal() {
		var dealer = (BlackjackDealer) publicTable.getDealer();
		assertNull(dealer.getTable().getPhase());
		assertTrue(takeSeat(1, blackjackPlayer));
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(0, blackjackPlayer.getHands().get(0).getCards().size());
		dealer.handlePlayerBet(blackjackPlayer, new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
	}

	@Test
	public void playersReceiveTwoCardsInFirstHandDuringInitialDeal() {
		var dealer = (BlackjackDealer) publicTable.getDealer();
		takeSeat(1, blackjackPlayer);
		takeSeat(2, blackjackPlayer2);
		dealer.handlePlayerBet(blackjackPlayer, new BigDecimal("54.0"));
		dealer.handlePlayerBet(blackjackPlayer2, new BigDecimal("51.0"));
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(0, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(1, blackjackPlayer2.getHands().size());
		assertEquals(0, blackjackPlayer2.getHands().get(0).getCards().size());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); //
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(2, blackjackPlayer2.getHands().get(0).getCards().size());
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(1, blackjackPlayer2.getHands().size());
	}

	@Test
	public void playerWithoutBetDoesNotGetCards() {
		assertTrue(takeSeat(1, blackjackPlayer));
		assertTrue(takeSeat(2, blackjackPlayer2));
		var dealer = (BlackjackDealer) publicTable.getDealer();
		dealer.handlePlayerBet(blackjackPlayer, new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(0, blackjackPlayer2.getHands().get(0).getCards().size());
	}

	@Test
	public void playersBetsAreAccepted() {
		assertTrue(takeSeat(1, blackjackPlayer));
		assertTrue(takeSeat(2, blackjackPlayer2));
		var dealer = (BlackjackDealer) publicTable.getDealer();
		dealer.handlePlayerBet(blackjackPlayer, new BigDecimal("50.0"));
		dealer.handlePlayerBet(blackjackPlayer2, new BigDecimal("99.7"));
		assertEquals("50.0", blackjackPlayer.getBet().toString());
		assertEquals("99.7", blackjackPlayer2.getBet().toString());
	}
}
