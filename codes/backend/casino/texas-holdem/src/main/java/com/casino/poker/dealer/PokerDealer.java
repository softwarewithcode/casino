package com.casino.poker.dealer;

import com.casino.common.dealer.CardDealer;
import com.casino.common.reload.Reloader;
import com.casino.poker.bet.BlindBetsHandler;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.pot.PokerPotHandler;
import com.casino.poker.pot.PotHandler;

import java.math.BigDecimal;

public interface PokerDealer extends CardDealer {


    BigDecimal calculateMinRaise();

    boolean isAnyChipsOnTable();

    void prepareNextGamePhase();

    void onPlayerLeave(PokerPlayer player);

    Reloader getReloader();

    void tearDown();

    BigDecimal countAllPlayersChipsOnTable();

    void continueGame(PokerPlayer player);

    void sitOut(PokerPlayer player, Boolean immediate);

    void onRoundStart();

    BlindBetsHandler getBlindsHandler();

    PotHandler getPotHandler();

}
