package com.casino.common.table.structure;

import com.casino.common.game.GameData;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableCard;
import com.casino.common.user.Connectable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ISeatedTable<T extends CasinoPlayer> extends CasinoTable {

	Set<Seat<T>> getSeats();

	T getPlayer(int seatNumber);

	T getPlayer(UUID playerId);

	List<Seat<T>> findInactivePlayerSeats();

	void verifyPlayerHasSeat(T player);

	boolean hasPlayers();

	void sanitizeSeat(int seatNumber);

	void sanitizeSeatsByPlayerStatus(List<PlayerStatus> playersStatuses);

	boolean isStatusAllowingPlayerEntries();

	Seat<T> getNextSeat(int startSeatNumber, boolean clockwise);

	List<T> getOrderedPlayersWithBet();

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

	void leave(UUID playerOrWatcherId);

	void watch(Connectable connectable);

	void refresh(UUID playerOrWatcherId);

	TableCard<? extends GameData> getTableCard();

	List<T> getPlayersWithStatus(List<PlayerStatus> anyOfStatuses);
}
