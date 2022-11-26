package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.player.IPlayer;

/*
 * For example blackjack and red dog games are order based games. 
 * Roulette is not order based as people tend to add chips at the same time.
 */
public abstract class OrderBasedTable extends BaseTable {

	private List<Seat> seats;

	protected OrderBasedTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type, int seats) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type);
		if (maxPlayers > seats)
			throw new IllegalArgumentException("not enough seats for the players");
		createSeats(seats);
	}

	private void createSeats(int seatCount) {
		List<Seat> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat(i)).collect(Collectors.toList());
		this.seats = Collections.synchronizedList(seats);
	}

	protected boolean takeSeat(int seatNumber, IPlayer player) {
		if (seatNumber <= 0 || seatNumber >= getMaxPlayers())
			return false;
		if (!coversMinimumBet(player))
			return false;
		if (super.isPrivate()) // In private table player can reserve all seats
			return seats.get(seatNumber).take(player);
		return hasSeat(player) ? false : seats.get(seatNumber).take(player);
	}

	public List<Seat> getSeats() {
		return seats;
	}

	private boolean hasSeat(IPlayer p) {
		if (p == null)
			return false;
		return seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

	protected void leaveSeat(Seat seat) {
		seat.leave();
	}
}
