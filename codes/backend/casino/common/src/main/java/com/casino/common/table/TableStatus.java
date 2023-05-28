package com.casino.common.table;

public enum TableStatus {
	RUNNING(true), WAITING_PLAYERS(true), CLOSED(false), CLOSING(false), ERROR(false);

	private boolean visible;

	private TableStatus(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return this.visible;
	}
}
