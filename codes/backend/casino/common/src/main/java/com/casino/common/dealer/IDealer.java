package com.casino.common.dealer;

import com.casino.common.player.ICasinoPlayer;

public interface IDealer {
	public void onPlayerArrival(ICasinoPlayer player);

	public boolean hasStartingAce();

}
