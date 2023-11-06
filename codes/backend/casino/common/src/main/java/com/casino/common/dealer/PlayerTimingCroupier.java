package com.casino.common.dealer;

import com.casino.common.player.CasinoPlayer;

public interface PlayerTimingCroupier extends Croupier {

    void onPlayerTimeout(CasinoPlayer player);

    default Integer getPlayerTurnTime() {
        return getTable().getDealer().getGameData().getPlayerTime();
    }

    default void onError() {
        Croupier.super.onError();
    }

}
