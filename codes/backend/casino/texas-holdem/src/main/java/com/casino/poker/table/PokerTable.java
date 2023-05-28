package com.casino.poker.table;

import com.casino.common.game.phase.GamePhase;
import com.casino.poker.dealer.PokerDealer;
import com.casino.poker.round.PokerRound;

import java.util.List;
import java.util.UUID;

public interface PokerTable<T> {

    PokerRound getRound();

    PokerRound getPreviousRound();

    List<T> getPlayers();

    PokerDealer getDealer();

    List<PokerRound> getRounds();

    UUID getId();

    GamePhase getGamePhase();
}
