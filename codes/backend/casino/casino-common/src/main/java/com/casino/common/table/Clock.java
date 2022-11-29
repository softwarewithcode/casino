package com.casino.common.table;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Clock {

	private Timer timer;
	private boolean ticking;

	public void startClock(TimerTask task, int millis) {
		stopClock();
		timer = new Timer(UUID.randomUUID().toString());
		timer.schedule(task, 0, millis);
		this.ticking = true;
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

	public Timer getTimer() {
		return timer;
	}
}
