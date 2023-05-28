package com.casino.poker.pot;

import com.casino.poker.player.PokerPlayer;

import java.math.BigDecimal;
import java.util.List;

public interface PotHandler {
    void onPhaseCompletion();

    void addTableChipsCount(BigDecimal amount, PokerPlayer player);

    void removePlayer(PokerPlayer player);

    List<Pot> completePots();

    List<Pot> getPots();

    void clearPots();

    BigDecimal getActivePotAmount();

    BigDecimal getActivePotAmountWithTableChips();

    void addToActivePot(BigDecimal amount);
}
