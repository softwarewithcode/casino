package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.player.ICasinoPlayer;

public class BlackjackTest extends BaseTest {

	@BeforeEach
	public void init() {
		publicTable = createPublicTable();
		blackjackPlayer = new BlackjackPlayer("PlayerName", UUID.randomUUID(), new BigDecimal("100.0"));
		blackjackPlayer2 = new BlackjackPlayer("Name", UUID.randomUUID(), new BigDecimal("100.0"));
	}

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

	private void takeSeat(int seatNumber, ICasinoPlayer player) {
		publicTable.join(player);
		publicTable.takeSeat(seatNumber, player);
	}

}
