package com.casino.blackjack.table;

import java.math.BigDecimal;

import com.casino.common.player.IPlayer;
import com.casino.common.table.OrderBasedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTable extends OrderBasedTable {

	protected BlackjackTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type, int seats) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onTimeout(IPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getComputerTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onPlayerLeave(IPlayer player) {
		// TODO Auto-generated method stub

	}

}
