package com.casino.poker.round.positions;

import com.casino.poker.player.PokerPlayer;

import java.util.List;

public record PokerRoundPlayers(PokerPlayer sb,
                                PokerPlayer bb,
                                PokerPlayer buttonPlayer,
                                Integer buttonSeatNumber,
                                Integer sbSeatNumber,
                                Integer bbSeatNumber,
                                List<PokerPlayer> players) {

    public PokerRoundPlayers {
        if (bb == null || buttonSeatNumber == null)
            throw new IllegalArgumentException("bigBlindPlayer and buttonSeat are required- Button:" + buttonSeatNumber);
        if (bb.equals(sb))
            throw new IllegalArgumentException("bigBlindPlayer cannot be smallBlindPlayer");
        if (bb.getSeatNumber().equals(buttonSeatNumber))
            throw new IllegalArgumentException("bigBlindPlayer cannot be buttonPlayer");
    }
}