package com.casino.common.table;

import com.casino.common.player.IPlayer;

public class Seat {
	private final int position;
	private IPlayer player;

	public Seat(int position) {
		this.position = position;
	}

	public boolean take(IPlayer p) {
		if (player != null)
			return false;
		player = p;
		return true;
	}

	public Seat(int position, IPlayer player) {
		super();
		this.position = position;
		this.player = player;
	}

	public void leave() {
		player = null;
	}

	public IPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IPlayer player) {
		this.player = player;
	}

	public int getPosition() {
		return position;
	}

	public boolean isAvailable() {
		return player == null;
	}

}
