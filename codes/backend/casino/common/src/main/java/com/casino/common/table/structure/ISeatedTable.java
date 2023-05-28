package com.casino.common.table.structure;

import java.util.Set;

import com.casino.common.player.ICasinoPlayer;

public interface ISeatedTable<T extends ICasinoPlayer> extends ICasinoTable {

	public Set<Seat<T>> getSeats();

	public Integer getActivePlayerCount();

	public T getPlayer(int seatNumber);
}
