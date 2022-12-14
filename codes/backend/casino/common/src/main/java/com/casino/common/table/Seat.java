package com.casino.common.table;

import java.util.Objects;
import java.util.Optional;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;

public class Seat {
	private final int number;
	private ICasinoPlayer player;

	public Seat(int number) {
		this.number = number;
	}

	public synchronized Optional<Seat> take(ICasinoPlayer newPlayer) {
		if (player != null)
			return Optional.empty();
		player = newPlayer;
		return Optional.of(this);
	}

	public boolean hasPlayerWithBet() {
		return hasPlayer() && this.player.hasBet();
	}

	public boolean hasPlayerWhoCanAct() {
		return hasPlayerWithBet() && this.player.getStatus() == PlayerStatus.ACTIVE && player.hasActiveHand();
	}

	public boolean hasPlayer() {
		return this.player != null;
	}

	public Seat(int number, ICasinoPlayer player) {
		super();
		this.number = number;
		this.player = player;
	}

	public void sanitize() {
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
	public String toString() {
		return "Seat [number=" + number + ", player=" + player + "]";
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
