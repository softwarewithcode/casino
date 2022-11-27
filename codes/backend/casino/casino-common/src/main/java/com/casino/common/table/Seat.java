package com.casino.common.table;

import java.util.Objects;

import com.casino.common.player.IPlayer;

public class Seat {
	private final int number;
	private IPlayer player;

	public Seat(int number) {
		this.number = number;
	}

	public boolean take(IPlayer p) {
		if (player != null)
			return false;
		player = p;
		return true;
	}

	public Seat(int number, IPlayer player) {
		super();
		this.number = number;
		this.player = player;
	}

	public boolean isEmpty() {
		return player == null;
	}

	public void leave() {
		player = null;
	}

	public boolean removePlayerIfHolder(IPlayer player) {
		if (this.player == null || player == null)
			return false;
		if (this.player.equals(player)) {
			this.player = null;
			return true;
		}
		return false;
	}

	public IPlayer getPlayer() {
		return player;
	}

	public void setPlayer(IPlayer player) {
		this.player = player;
	}

	public int getNumber() {
		return number;
	}

	public boolean isAvailable() {
		return player == null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(number);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Seat other = (Seat) obj;
		return number == other.number;
	}

}
