package com.casino.common.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.bet.BetUtil;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.phase.PhasePath;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author softwarewithcode from GitHub
 *
 */
/*
 * For example blackjack and red dog games are order based games. Roulette is
 * not order based as people tend to add chips at the same time.
 */
public abstract class SeatedTable extends CasinoTable implements ISeatedTable {
	@JsonIgnore
	private Set<Seat> seats;

	protected SeatedTable(Status initialStatus, TableInitData tableDescription, PhasePath phasePath) {
		super(initialStatus, tableDescription, phasePath);
		if (tableDescription.thresholds().maxPlayers() > tableDescription.thresholds().seatCount())
			throw new IllegalArgumentException("not enough seats for the players");
		createSeats(tableDescription.thresholds().seatCount());
	}

	private void createSeats(int seatCount) {
		Set<Seat> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat(i)).collect(Collectors.toSet());
		this.seats = Collections.synchronizedSet(seats);
	}

	@Override
	public Integer getReservedSeatCount() {
		return (int) seats.stream().filter(Seat::hasPlayer).count();
	}

	@Override
	public Integer getActivePlayerCount() {
		return (int) seats.stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != com.casino.common.player.PlayerStatus.SIT_OUT).count();
	}

	// User might have disconnected, so don't compare to active status
	public List<ICasinoPlayer> getPlayersWithBet() {
		return seats.stream().filter(seat -> seat.hasPlayerWithBet()).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public boolean hasPlayersWithWinningChances() {
		return seats.stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().hasWinningChance()).findFirst().isPresent();
	}

	@Override
	public Optional<Seat> trySeat(Integer seatNumber, ICasinoPlayer player) {
		Seat seat = null;
		BetUtil.verifySufficentBalance(getThresholds().minimumBet(), player);
		if (!isAcceptingPlayers())
			return Optional.empty();
		if (hasSeat(player))
			return Optional.empty();
		if (seatNumber == null) {
			seat = seats.stream().filter(Seat::isAvailable).findAny().get();
		} else if (seatNumber < 0 || seatNumber >= seats.size())
			throw new IllegalArgumentException("seat number is incorrect " + seatNumber + " has:" + getSeats().size());
		else
			seat = seats.stream().filter(s -> s.getNumber() == seatNumber).findFirst().get();
		return seat.take(player);
	}

	@Override
	public ICasinoPlayer getPlayer(int seatNumber) {
		if (seatNumber < 0 || seatNumber > seats.size() - 1)
			return null;
		Optional<Seat> optionalSeat = getSeats().stream().filter(seat -> seat.getNumber() == seatNumber).findAny();
		return optionalSeat.isPresent() ? optionalSeat.get().getPlayer() : null;
	}

	@SuppressWarnings("unchecked")
	public <T extends ICasinoPlayer> T getPlayer(UUID playerId) {
		if (playerId == null)
			return null;
		Optional<Seat> optionalSeat = getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getId().equals(playerId)).findAny();
		return optionalSeat.isPresent() ? (T) optionalSeat.get().getPlayer() : null;
	}

	@Override
	public void leaveSeats(ICasinoPlayer player) {
		// in a private table user can reserve all the seats
		seats.forEach(seat -> seat.removePlayerIfHolder(player));
	}

	protected void sanitizeAllSeats() {
		seats.stream().forEach(seat -> seat.sanitize());
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	@JsonProperty("seats")
	public List<Seat> getSeatsAsList() {
		return new ArrayList<>(seats);
	}

	public boolean hasSeat(ICasinoPlayer p) {
		return p == null ? false : seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
	}

	public List<ICasinoPlayer> getPlayers() {
		return seats.stream().filter(Seat::hasPlayer).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public boolean hasPlayers() {
		return getReservedSeatCount() != 0;
	}

	public List<Seat> findInActivePlayerSeats() {
		return getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != PlayerStatus.ACTIVE).toList();
	}

}
