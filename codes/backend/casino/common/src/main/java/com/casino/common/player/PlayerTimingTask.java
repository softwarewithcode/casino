package com.casino.common.player;

import java.util.TimerTask;

import com.casino.common.dealer.PlayerTimingCroupier;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.table.timing.TimeControl;

public class PlayerTimingTask extends TimerTask {
	private final CasinoTable table;
	private final CasinoPlayer player;
	private final TimeControl playerTimeControl;

	public PlayerTimingTask(PlayerTimingCroupier croupier, CasinoPlayer playerRunningOutOfTurnTime) {
		this.table = croupier.getTable();
		playerTimeControl = playerRunningOutOfTurnTime.getTimeControl();
		player = playerRunningOutOfTurnTime;
		table.updateCounterTime(croupier.getPlayerTurnTime());
	}

	@Override
	public void run() {
		if (!table.isClockTicking())
			return;
		Integer timeLeft = playerTimeControl.reduceSecond(table.getGamePhase().usesTimeBank());
		if (timeLeft == 0) {
			table.stopTiming();
			((PlayerTimingCroupier)table.getDealer()).onPlayerTimeout(player);
		}
	}
}
