package com.casino.common.table;

import java.util.Optional;
import java.util.Set;

import com.casino.common.player.ICasinoPlayer;

public interface ISeatedTable extends ICasinoTable {
	public Optional<Seat> trySeat(Integer seatNumber, ICasinoPlayer player);

	public Set<Seat> getSeats();

	public Integer getReservedSeatCount();

	public Integer getActivePlayerCount();

	public void leaveSeats(ICasinoPlayer player);

	public ICasinoPlayer getPlayer(int seatNumber);
}
