package com.casino.common.player;

import java.util.TimerTask;

import com.casino.common.table.structure.ICasinoTable;
import com.casino.common.table.timing.TimeControl;

public class PlayerTime extends TimerTask {
	private final ICasinoTable table;
	private final ICasinoPlayer player;
	private final TimeControl playerTimeControl;

	public PlayerTime(ICasinoTable table, ICasinoPlayer playerRunningOutOfTurnTime) {
		this.table = table;
		playerTimeControl = playerRunningOutOfTurnTime.getTimeControl();
		player = playerRunningOutOfTurnTime;
		table.updateCounterTime(table.getDealer().getPlayerTurnTime());
	}

	@Override
	public void run() {
		if (!table.isClockTicking())
			return;
		Integer timeLeft = playerTimeControl.reduceSecond(table.getGamePhase().usesTimeBank());
		if (timeLeft == 0) {
			table.stopClock();
			table.getDealer().onPlayerTimeout(player);
		}
	}
}
