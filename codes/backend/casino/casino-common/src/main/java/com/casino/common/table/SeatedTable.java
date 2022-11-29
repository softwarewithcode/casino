package com.casino.common.table;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.bet.BetUtil;
import com.casino.common.bet.BetValues;
import com.casino.common.player.ICasinoPlayer;

/*
 * For example blackjack and red dog games are order based games. 
 * Roulette is not order based as people tend to add chips at the same time.
 */
public abstract class SeatedTable extends CasinoTable implements ISeatedTable {

	private Set<Seat> seats;
	private ICasinoPlayer playerInTurn;

	protected SeatedTable(Status initialStatus, BetValues betLimit, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betLimit, playerLimit, type, id);
		if (playerLimit.maximumPlayers() > seats)
			throw new IllegalArgumentException("not enough seats for the players");
		createSeats(seats);
	}

	private void createSeats(int seatCount) {
		Set<Seat> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat(i)).collect(Collectors.toSet());
		this.seats = Collections.synchronizedSet(seats);
	}

	@Override
	public Integer getReservedSeatCount() {
		return (int) seats.stream().filter(seat -> seat.getPlayer() != null).count();
	}

	@Override
	public Integer getActivePlayerCount() {
		return (int) seats.stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getStatus() != com.casino.common.player.Status.SIT_OUT).count();
	}

	@Override
	public boolean takeSeat(int seatNumber, ICasinoPlayer player) {
		BetUtil.verifySufficentAmount(getBetValues().minimumBet(), player);
		if (seatNumber < 0 || seatNumber >= seats.size())
			return false;
		if (super.isPrivate()) { // In private table player can reserve all seats
			return trySeat(seatNumber, player);
		}
		if (hasAlreadySeat(player))
			return false;
		boolean gotSeat = trySeat(seatNumber, player);
		if (gotSeat)
			getDealer().handleNewPlayer(player);
		return gotSeat;
	}

	@Override
	public void leaveSeats(ICasinoPlayer player) {
		// in a private table user can take all the seats
		seats.forEach(seat -> seat.removePlayerIfHolder(player));
	}

	private boolean trySeat(int seatNumber, ICasinoPlayer player) {
		Seat seat = seats.stream().filter(s -> s.getNumber() == seatNumber).findFirst().orElse(null);
		if (seat == null || !seat.take(player))
			return false;
		super.changeFromWatcherToPlayer(player);
		return true;
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	private boolean hasAlreadySeat(ICasinoPlayer p) {
		return p == null ? false : seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

	@Override
	public ICasinoPlayer getPlayerInTurn() {
		return playerInTurn;
	}

	@Override
	public boolean join(ICasinoPlayer player) {
		return super.joinAsWatcher(player);
	}
}
