package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.player.IPlayer;

/*
 * For example blackjack and red dog games are order based games. 
 * Roulette is not order based as people tend to add chips at the same time.
 */
public abstract class SeatedTable extends CasinoTable implements ISeatedTable {

	private Set<Seat> seats;

	protected SeatedTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type, int seats, UUID id) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type, id);
		if (maxPlayers > seats)
			throw new IllegalArgumentException("not enough seats for the players");
		createSeats(seats);
	}

	private void createSeats(int seatCount) {
		Set<Seat> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat(i)).collect(Collectors.toSet());
		this.seats = Collections.synchronizedSet(seats);
	}

	@Override
	public boolean takeSeat(int seatNumber, IPlayer player) {
		if (seatNumber <= 0 || seatNumber >= seats.size())
			return false;
		if (!coversMinimumBet(player))
			return false;
		if (super.isPrivate()) { // In private table player can reserve all seats
			takeSeatIfAvailable(seatNumber, player);
		}
		return hasSeat(player) ? false : takeSeatIfAvailable(seatNumber, player);
	}

	@Override
	public void leaveSeats(IPlayer player) {
		// Less than 10 possibility
		seats.forEach(seat -> seat.removePlayerIfHolder(player));
	}

	private boolean takeSeatIfAvailable(int seatNumber, IPlayer player) {
		Seat seat = seats.stream().filter(s -> s.getNumber() == seatNumber).findFirst().orElse(null);
		return seat == null ? false : seat.take(player);
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	private boolean hasSeat(IPlayer p) {
		return p == null ? false : seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

}
