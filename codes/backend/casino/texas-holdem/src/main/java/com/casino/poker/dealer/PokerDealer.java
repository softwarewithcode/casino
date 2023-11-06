package com.casino.poker.dealer;

import com.casino.common.dealer.CardDealer;
import com.casino.common.reload.Reloader;
import com.casino.poker.bet.BlindBetsHandler;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.pot.PotHandler;

import java.math.BigDecimal;

public interface PokerDealer extends CardDealer {

    BigDecimal calculateMinRaise();

    boolean isAnyChipsOnTable();

    Reloader getReloader();

    void tearDownTable();

    BigDecimal countAllPlayersChipsOnTable();

    void handleReturningPlayer(PokerPlayer player);

    void handleSitOut(PokerPlayer player, Boolean immediate);

    void onRoundStart();

    BlindBetsHandler getBlindsHandler();

    PotHandler getPotHandler();

    void handleBetOrRaise(PokerPlayer player, BigDecimal raiseToAmount);

    void handleFold(PokerPlayer player);

    void handleCheck(PokerPlayer player);

    void handleCall(PokerPlayer player);
}
