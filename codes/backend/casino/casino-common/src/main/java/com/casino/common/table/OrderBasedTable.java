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

	protected List<Seat> seats;

	protected OrderBasedTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type);
		createSeats(maxPlayers);
	}

	private void createSeats(int maxPlayers) {
		List<Seat> seats = IntStream.range(0, maxPlayers).mapToObj(i -> new Seat(i)).collect(Collectors.toList());
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


	private boolean hasSeat(IPlayer p) {
		if (p == null)
			return false;
		return seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

	protected void leaveSeat(Seat seat) {
		seat.leave();
	}
}
