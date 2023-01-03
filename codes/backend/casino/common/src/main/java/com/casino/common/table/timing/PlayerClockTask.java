package com.casino.common.table.timing;

import java.util.TimerTask;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;

public class PlayerClockTask extends TimerTask {
	private ICasinoTable table;
	private ICasinoPlayer player;
	private int secondsLeft;

	public PlayerClockTask(ICasinoTable table, ICasinoPlayer playerRunningOutOfTurnTime) {
		this.table = table;
		secondsLeft = table.getPlayerTurnTime();
		player = playerRunningOutOfTurnTime;
		table.updateCounterTime(table.getThresHolds().playerTime());
	}

	@Override
	public void run() {
		if (!table.isClockTicking()) {
			return;
		}
		secondsLeft--;
		if (secondsLeft == 0) {
			table.stopClock();
			table.onPlayerTimeout(player);
		}
	}
}
