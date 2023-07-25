package com.casino.common.table.structure;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface ISeatedTable<T extends ICasinoPlayer> extends ICasinoTable {

    Set<Seat<T>> getSeats();

    T getPlayer(int seatNumber);

    default Integer getActivePlayerCount() {
        return (int) getSeats().stream().filter(Seat::hasActivePlayer).count();
    }

    default Integer getReservedSeatCount() {
        return (int) getSeats().stream().filter(Seat::hasPlayer).count();
    }

    default Integer getPlayerCount() {
        return (int) getSeats().stream().filter(Seat::hasPlayer).count();
    }

    default Integer getNewPlayerCount() {
        return (int) getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() == PlayerStatus.NEW).count();
    }

    default List<Seat<T>> getNewAndActivePlayersSeatsCoveringAmount(BigDecimal requiredAmount) {
        return getSeats().stream().filter(seat -> seat.hasNewPlayerCoveringAmount(requiredAmount) || seat.hasActivePlayerCoveringAmount(requiredAmount)).sorted(Comparator.comparing(Seat::getNumber)).toList();
    }

    // User might have disconnected, so don't compare to active status
    default List<T> getPlayersWithBet() {
        return getSeats().stream().filter(Seat::hasPlayerWithBet).map(Seat::getPlayer).toList();
    }

}
