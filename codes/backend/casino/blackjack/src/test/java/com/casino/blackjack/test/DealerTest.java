package com.casino.blackjack.test;

import org.junit.jupiter.api.Test;

import com.casino.blackjack.rules.BlackjackDealer;

public class DealerTest extends BaseTest {
	@Test
	public void betRoundStartsAfterFirstPlayerTakesSeat() {
		takeSeat(1, blackjackPlayer);
		var dealer = (BlackjackDealer) publicTable.getDealer();
		dealer.welcomeNewPlayer(blackjackPlayer);
		sleep(BET_ROUND_TIME * 1000 + 3 * 1000); // Wait 3*1000 milliseconds after betRound has ended
	}

}
