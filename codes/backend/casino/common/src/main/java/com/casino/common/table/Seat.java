package com.casino.common.table;

import java.util.Objects;

import com.casino.common.player.ICasinoPlayer;

public class Seat {
	private final int number;
	private ICasinoPlayer player;

	public Seat(int number) {
		this.number = number;
	}

	public synchronized boolean take(ICasinoPlayer p) {
		if (player != null)
			return false;
		player = p;
		return true;
	}

	public Seat(int number, ICasinoPlayer player) {
		super();
		this.number = number;
		this.player = player;
	}

	public void leave() {
		player = null;
	}

	public boolean removePlayerIfHolder(ICasinoPlayer player) {
		if (this.player == null || player == null)
			return false;
		if (this.player.equals(player)) {
			this.player = null;
			return true;
		}
		return false;
	}

	public ICasinoPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ICasinoPlayer player) {
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
