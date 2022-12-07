package com.casino.common.table.timing;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Clock {

	private Timer timer;
	private boolean ticking;

	public void startClock(TimerTask task, int millis) {
		stopClock();
		this.ticking = true;
		timer = new Timer(UUID.randomUUID().toString());
		timer.schedule(task, 0, millis);
	}

	public void stopClock() {
		this.ticking = false;
		if (this.timer != null) {
			timer.cancel();
		}
	}

	public boolean isTicking() {
		return ticking;
	}

}
