package com.casino.common.table;

public enum Status {
	RUNNING(true), WAITING_PLAYERS(true), CLOSED(false), CLOSING(false);

	private boolean visible;

	private Status(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return this.visible;
	}
}
