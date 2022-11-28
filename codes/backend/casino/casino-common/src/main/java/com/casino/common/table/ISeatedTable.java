package com.casino.common.table;

import java.util.Set;

import com.casino.common.player.ICasinoPlayer;

public interface ISeatedTable extends ICasinoTable {
	public boolean takeSeat(int seatNumber, ICasinoPlayer player);

	public Set<Seat> getSeats();

	public Integer getReservedSeatCount();

	public void leaveSeats(ICasinoPlayer player);
}
