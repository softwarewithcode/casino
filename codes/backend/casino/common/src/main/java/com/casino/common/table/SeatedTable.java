package com.casino.common.table;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.bet.Thresholds;
import com.casino.common.bet.BetUtil;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.phase.PhasePath;

/*
 * For example blackjack and red dog games are order based games. 
 * Roulette is not order based as people tend to add chips at the same time.
 */
public abstract class SeatedTable extends CasinoTable implements ISeatedTable {

	private Set<Seat> seats;

	protected SeatedTable(Status initialStatus, Thresholds tableConstants, UUID tableId, PhasePath phasePath) {
		super(initialStatus, tableConstants, tableId, phasePath);
		if (tableConstants.maxPlayers() > tableConstants.seatCount())
			throw new IllegalArgumentException("not enough seats for the players");
		createSeats(tableConstants.seatCount());
	}

	private void createSeats(int seatCount) {
		Set<Seat> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat(i)).collect(Collectors.toSet());
		this.seats = Collections.synchronizedSet(seats);
	}

	@Override
	public Integer getReservedSeatCount() {
		return (int) seats.stream().filter(seat -> seat.hasPlayer()).count();
	}

	@Override
	public Integer getActivePlayerCount() {
		return (int) seats.stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != com.casino.common.player.Status.SIT_OUT).count();
	}

	public boolean hasWaitingPlayers() {
		return seats.stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().hasWinningChance()).findFirst().isPresent();
	}

	public Seat getNextPlayerWithActiveActiveHand() {
		Optional<Seat> playerInTurnOptional = seats.stream().filter(seat -> !seat.isAvailable() && seat.getPlayer().equals(getPlayerInTurn())).findFirst();
		if (playerInTurnOptional.isEmpty())
			throw new IllegalStateException("No playerInTurn");
		Seat playerInTurnSeat = playerInTurnOptional.get();
		Optional<Seat> nextPlayer = seats.stream().filter(seat -> isNextSeatWithBet(playerInTurnSeat, seat)).findFirst();
		return nextPlayer.orElse(null);
	}

	private boolean isNextSeatWithBet(Seat playerInTurnSeat, Seat seat) {
		return !seat.isAvailable() && seat.getPlayer().hasActiveHand() && seat.getNumber() > playerInTurnSeat.getNumber();
	}

	// public trySeat(..) vs. required join() first ?
	@Override
	public boolean trySeat(int seatNumber, ICasinoPlayer player) {
		BetUtil.verifySufficentBalance(getThresholds().minimumBet(), player);
		if (!isAcceptingPlayers())
			return false;
		if (seatNumber < 0 || seatNumber >= seats.size())
			return false;
		if (hasAlreadySeat(player))
			return false;
		Seat seat = seats.stream().filter(s -> s.getNumber() == seatNumber).findFirst().orElse(null);
		if (seat == null || !seat.take(player))
			return false;
		super.changeFromWatcherToPlayer(player);
		return true;
	}

	@Override
	public boolean watch(ICasinoPlayer player) {
		return super.joinAsWatcher(player);
	}

	@Override
	public void leaveSeats(ICasinoPlayer player) {
		// in a private table user can take all the seats
		seats.forEach(seat -> seat.removePlayerIfHolder(player));
	}

	protected void sanitizeAllSeats() {
		seats.stream().map(seat -> seat.getPlayer()).forEach(player -> {
			super.changeFromPlayerToWatcher(player);
		});
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	private boolean hasAlreadySeat(ICasinoPlayer p) {
		return p == null ? false : seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

}
