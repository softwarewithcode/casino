package com.casino.common.dealer;

import com.casino.common.player.CasinoPlayer;

public interface IDealer {
	public <T extends CasinoPlayer> void onPlayerArrival(T player);

}
