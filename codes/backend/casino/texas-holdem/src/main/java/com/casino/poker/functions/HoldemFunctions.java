package com.casino.poker.functions;

import com.casino.common.bet.Range;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.functions.Functions;
import com.casino.common.player.PlayerStatus;
import com.casino.poker.actions.PokerAction;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.dealer.HoldemDealer;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.table.HoldemTable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class HoldemFunctions {
    public final static BiFunction<PokerPlayer, PokerActionType, Boolean> hasAction =
            (player, requiredAction) -> player.getActions()
                    .stream()
                    .map(PokerAction::getType)
                    .anyMatch(action -> action == requiredAction);
    public final static BiFunction<PokerPlayer, PokerActionType, Optional<PokerAction>> getActionType =
            (player, requiredAction) -> player.getActions()
                    .stream()
                    .filter(action -> action.getType() == requiredAction)
                    .findAny();

    public final static BiPredicate<HoldemPlayer, HoldemPlayer> higherSeatNumber = (startingPlayer, comparisonPlayer) -> comparisonPlayer.getSeatNumber() > startingPlayer.getSeatNumber();
    public final static BiPredicate<HoldemPlayer, HoldemPlayer> lowerSeatNumber = (startingPlayer, comparisonPlayer) -> comparisonPlayer.getSeatNumber() < startingPlayer.getSeatNumber();
    public final static BiPredicate<Integer, Integer> higherSeatNmber = (startingPlayer, comparisonPlayer) -> comparisonPlayer > startingPlayer;
    public final static BiPredicate<Integer, Integer> lowerSeatNmber = (startingPlayer, comparisonPlayer) -> comparisonPlayer < startingPlayer;

    public static List<PokerPlayer> getPlayersInSeatOrderStartingFromYYYY(PokerPlayer startPlayer, List<PokerPlayer> players, List<PlayerStatus> allowedStatuses) {
        return HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(startPlayer.getSeatNumber(), players, allowedStatuses);
    }

    public static List<PokerPlayer> getPlayersSittingInBetweenSeatsWithStatus(int startSeatNumberExclusive, int endSeatNumberExclusive, List<PokerPlayer> roundPlayers, List<PlayerStatus> allowedStatuses) {
        List<PokerPlayer> firstSeatExcludedPlayers = getPlayersWithStatusInOrderByStartingSeatNumber(startSeatNumberExclusive, roundPlayers, allowedStatuses)
                .stream().filter(player -> !player.getSeatNumber().equals(startSeatNumberExclusive)).toList();
        return firstSeatExcludedPlayers.stream().takeWhile(player -> !player.getSeatNumber().equals(endSeatNumberExclusive)).toList();
    }

    public static List<PokerPlayer> getPlayersWithStatusInOrderByStartingSeatNumber(int startSeatNumber, List<PokerPlayer> players, List<PlayerStatus> allowedStatuses) {
        List<PokerPlayer> finalPlayers = players.stream()
                .filter(player -> allowedStatuses.contains(player.getStatus()))
                .filter(player -> higherSeatNmber.test(startSeatNumber, player.getSeatNumber()))
                .collect(Collectors.toList());

        List<PokerPlayer> lowerSeatNumberPlayers = players.stream()
                .filter(player -> allowedStatuses.contains(player.getStatus()))
                .filter(player -> lowerSeatNmber.test(startSeatNumber, player.getSeatNumber()))
                .toList();
        finalPlayers.addAll(lowerSeatNumberPlayers);

        Optional<PokerPlayer> startingPlayer = players.stream().filter(p -> p.getSeatNumber() == startSeatNumber).findFirst();
        if (startingPlayer.isPresent()) {
            PokerPlayer p = startingPlayer.get();
            if (allowedStatuses.contains(p.getStatus()))
                finalPlayers.add(0, p);
        }
        return finalPlayers;
    }

    public final static boolean hasAnybodyMoreChipsOnTable(BigDecimal chipsOnTable, HoldemTable table) {
        return table.getRound().getPlayers().stream().filter(hasPlayerMoreChipsOnTable(chipsOnTable)).toList().size() > 0;
    }

    private final static Predicate<PokerPlayer> hasPlayerMoreChipsOnTable(BigDecimal chipsOnTable) {
        return player -> Functions.isFirstMoreThanSecond.apply(player.getTableChipCount(), chipsOnTable);
    }
    public final static void verifyRaiseIsTechnicallyCorrect(PokerPlayer player, BigDecimal raiseAmount, HoldemTable table) {
        PokerAction action = HoldemFunctions.getActionType.apply(player, PokerActionType.BET_RAISE).orElseThrow(() -> new IllegalPlayerActionException("BetOrRaise is not allowed action:" + player.getUserName()));
        Range raiseRange = action.getRange();
        if (!raiseRange.isInRange(raiseAmount))
            throw new IllegalBetException("Raise " + raiseAmount + " is not in allowed range. InitialRaiseAmount=" + table.getRound().getInitialRaiseAmount() + ", minimumTo=" + raiseRange.min(), 1);
        if (Functions.isFirstMoreOrEqualToSecond.apply(raiseAmount, HoldemDealer.GLOBAL_RESTRICTION_OF_MAXIMUM_RAISE))
            throw new IllegalBetException("Raise amount is too much for world economy " + raiseAmount + " initialRaiseAmount=" + table.getRound().getInitialRaiseAmount(), 2);
        if (Functions.isFirstMoreOrEqualToSecond.apply(BigDecimal.ZERO, raiseAmount))
            throw new IllegalBetException("Technical error negative amount:" + raiseAmount + "initialRaiseAmount=" + table.getRound().getInitialRaiseAmount(), 3);
    }

}
