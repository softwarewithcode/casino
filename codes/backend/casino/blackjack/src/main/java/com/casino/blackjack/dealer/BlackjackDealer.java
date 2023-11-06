package com.casino.blackjack.dealer;

import java.math.BigDecimal;

import com.casino.blackjack.player.BlackjackPlayer_;
import com.casino.common.dealer.CardDealer;
import com.casino.common.player.CasinoPlayer;

public interface BlackjackDealer extends CardDealer {
    void handleInsure(BlackjackPlayer_ player);

    void handleDoubleDown(BlackjackPlayer_ player);

    void handleStand(BlackjackPlayer_ player);

    void handleHit(BlackjackPlayer_ player);

    void handleSplit(BlackjackPlayer_ player);

    void handleBet(CasinoPlayer tablePlayer, BigDecimal bet);
}
