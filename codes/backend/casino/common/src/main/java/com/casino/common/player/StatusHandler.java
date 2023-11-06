package com.casino.common.player;

import java.util.Objects;
import java.util.UUID;

import com.casino.common.table.structure.CasinoTable;

public class StatusHandler {

	public static void verifyPlayerHasStatus(CasinoTable table, UUID playerId, PlayerStatus expectedStatus) {
		Objects.requireNonNull(table);
		Objects.requireNonNull(playerId);
		Objects.requireNonNull(expectedStatus);
		CasinoPlayer player = table.getPlayers().stream().filter(searchedPlayer -> searchedPlayer.getId() == playerId).findFirst().orElseThrow();
		if (player.getStatus() != expectedStatus)
			throw new IllegalStateException("Player " + player.getUserName() + " status is:" + player.getStatus() + " but was expected to be:" + expectedStatus);
	}
}
