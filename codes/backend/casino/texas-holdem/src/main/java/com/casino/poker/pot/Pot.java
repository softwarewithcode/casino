package com.casino.poker.pot;

import java.math.BigDecimal;
import java.util.List;

import com.casino.poker.player.PokerPlayer;

public interface Pot {

    void removePlayer(PokerPlayer player);

    List<? extends PokerPlayer> getWinners();

    void setWinners(List<PokerPlayer> potWinners);

    void setPlayers(List<PokerPlayer> players);

    BigDecimal getAmount();

    List<PokerPlayer> getPlayers();

    void seal();

    boolean isSealed();

    void add(BigDecimal additionalAmount);
    void addTableChips(BigDecimal additionalAmount);

    void deductRake(BigDecimal rakeAmount);

    BigDecimal getRake();

    boolean isCompleted();

    void complete();

    void clearTableChips();

    BigDecimal getAmountWithTableChips();
}
