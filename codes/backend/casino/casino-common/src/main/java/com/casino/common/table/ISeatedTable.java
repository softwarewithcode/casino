package com.casino.common.table;

import java.util.Set;

import com.casino.common.player.IPlayer;

public interface ISeatedTable extends ICasinoTable {
	public boolean takeSeat(int seatNumber, IPlayer player);

	public Set<Seat> getSeats();

	public void leaveSeats(IPlayer player);
}
