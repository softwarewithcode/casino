package com.casino.poker.bet;

import com.casino.poker.player.PokerPlayer;
import com.casino.poker.table.PokerTable;


public interface BlindBetsHandler {
    void clearMissedBlinds(PokerPlayer player);

    void addMissingBlindBetToken(PokerPlayer player, BetToken token);

    boolean isMissingSmallBlind(PokerPlayer player);

    boolean isMissingBigBlind(PokerPlayer player);

    boolean isMissingBothBlinds(PokerPlayer player);

    boolean isMissingAnyBlind(PokerPlayer player);

    void postBigBlind(PokerPlayer player);

    void postSmallBlind(PokerPlayer player);

    void collectMissedBlinds(PokerTable<PokerPlayer> table);

    void collectBlinds(PokerTable<PokerPlayer> table);

    void handleBlindBets(PokerTable<PokerPlayer> table);
}
