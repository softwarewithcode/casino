package com.casino.common.dealer;

import com.casino.common.player.ICasinoPlayer;

public interface IDealer {
	public void handleNewPlayer(ICasinoPlayer player);

	public boolean hasStartingAce();

}
