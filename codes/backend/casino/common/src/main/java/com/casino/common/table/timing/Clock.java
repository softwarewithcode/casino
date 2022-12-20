package com.casino.common.table.timing;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Clock {

	private Timer timer;
	private boolean ticking;
	private int timeLeft;

	public void startClock(TimerTask task, long initialDelay, int millis) {
		stopClock();
		this.ticking = true;
		timer = new Timer(UUID.randomUUID().toString());
		System.out.println("StartClock with delay:" + initialDelay);
		timer.schedule(task, initialDelay, millis);
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

	public void updateTime(int time) {
		this.timeLeft = time;
	}

	public int getTime() {
		return timeLeft;
	}

}
