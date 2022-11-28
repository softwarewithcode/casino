package com.casino.blackjack.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlackjackTest extends BaseTest {

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		publicTable.join(blackjackPlayer);
		Assertions.assertEquals(1, publicTable.getWatchers().size());
		Assertions.assertEquals(0, publicTable.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		takeSeat(0, blackjackPlayer);
		Assertions.assertEquals(0, publicTable.getWatchers().size());
		Assertions.assertEquals(1, publicTable.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		takeSeat(0, blackjackPlayer);
		takeSeat(0, blackjackPlayer2);
		Assertions.assertEquals(blackjackPlayer, publicTable.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer());
	}

}
