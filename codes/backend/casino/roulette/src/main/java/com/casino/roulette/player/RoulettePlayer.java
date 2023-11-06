package com.casino.roulette.player;

import com.casino.common.player.CasinoPlayer;
import com.casino.common.ranges.Range;
import com.casino.roulette.bet.RouletteBet;
import com.casino.roulette.persistence.RoundResult;
import com.casino.roulette.persistence.SpinResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RoulettePlayer extends CasinoPlayer {
    void bet(RouletteBet bet, Range<BigDecimal> minMax);

    void removeBets(Boolean removeAllBets);

    void removeBetsFromPosition(Integer position);

    List<RouletteBet> getBets();

    BigDecimal getTotalOnTable();

    void repeatLastBets(Range<BigDecimal> minMax);

    void updateBalanceAndWinnings(Integer winNumber);

    List<RoundResult> getRoundResults();

    void onRoundCompletion(SpinResult data);

    Map<Integer, BigDecimal> getPositionsTotalAmounts();
}
