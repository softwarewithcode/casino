package com.casino.common.table.structure;

import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.functions.Functions;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableData;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author softwarewithcode from GitHub
 */
/*
 * For example blackjack and red dog games are order based games. Roulette is not order based as people tend to add chips at the same time.
 */
@JsonIncludeProperties(value = { /* implementing subclass defines exact fields */})
public abstract class SeatedTable<T extends ICasinoPlayer> extends CasinoTable implements ISeatedTable<T> {
    private Set<Seat<T>> seats;

    protected SeatedTable(TableData initData) {
        super(initData);
        if (initData.thresholds().maxPlayers() > initData.thresholds().seatCount())
            throw new IllegalArgumentException("not enough seats for the players");
        createSeats(initData.thresholds().seatCount());
    }

    private void createSeats(int seatCount) {
        Set<Seat<T>> seats = IntStream.range(0, seatCount).mapToObj(i -> new Seat<T>(i)).collect(Collectors.toSet());
        this.seats = Collections.synchronizedSet(seats);
    }

    protected Optional<Seat<T>> join(String seatNumber, T player) {
        Integer seatNumbr = seatNumber != null ? Integer.parseInt(seatNumber) : null;
        Optional<Seat<T>> seatOptional = trySeat(seatNumbr, player);
        if (seatOptional.isPresent()) {
            super.removeWatcher(player.getId());
        }
        return seatOptional;
    }

    private Optional<Seat<T>> trySeat(Integer seatNumber, T player) {
        Seat<T> seat;
        BetVerifier.verifySufficientBalance(getDealer().getGameData().getMinBuyIn(), player);
        if (!isStatusAllowingPlayerEntries())
            return Optional.empty();
        if (isReservedSinglePlayerTable())
            return Optional.empty();
        if (hasSeat(player))
            return Optional.empty();
        if (shouldSearchAnyFreeSeat(seatNumber))
            seat = seats.stream().filter(Seat::isAvailable).findAny().orElseThrow(IllegalArgumentException::new);
        else if (seatNumber < 0 || seatNumber >= seats.size())
            throw new IllegalArgumentException("seat number is incorrect " + seatNumber + " table total:" + getSeats().size());
        else
            seat = seats.stream().filter(s -> Objects.equals(s.getNumber(), seatNumber)).findAny().orElseThrow(IllegalArgumentException::new);
        return seat.take(player);
    }

    private boolean isReservedSinglePlayerTable() {
        return getType() == TableType.SINGLE_PLAYER && getPlayers().size() != 0;
    }

    private boolean shouldSearchAnyFreeSeat(Integer seatNumber) {
        return seatNumber == null;
    }

    @Override
    public T getPlayer(int seatNumber) { // TO optional
        if (seatNumber < 0 || seatNumber > seats.size() - 1)
            return null;
        Optional<Seat<T>> optionalSeat = getSeats().stream().filter(seat -> seat.getNumber().equals(seatNumber)).findAny();
        return optionalSeat.map(Seat::getPlayer).orElse(null);
    }

    public T getPlayer(UUID playerId) { // TO optional
        if (playerId == null)
            return null;
        Optional<Seat<T>> optionalSeat = getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getId().equals(playerId)).findAny();
        return optionalSeat.map(Seat::getPlayer).orElse(null);
    }

    protected void sanitizeAllSeats() {
        List<Seat<T>> allSeats = seats.stream().toList();
        allSeats.forEach(Seat::sanitize);
    }

    public void updatePlayersToWatchers(boolean sanitizeAllSeats) {
        List<Seat<T>> seatsToSanitize;
        if (sanitizeAllSeats)
            seatsToSanitize = seats.stream().toList();
        else
            seatsToSanitize = seats.stream().filter(Seat::hasPlayerWhoShouldStandUp).toList();
        seatsToSanitize.forEach(seat -> super.addWatcher(seat.getPlayer()));
        seatsToSanitize.forEach(Seat::sanitize);
    }

    public Set<Seat<T>> getSeats() {
        return seats;
    }

    public Seat<T> getNextSeat(int startSeatNumber, boolean clockwise) {
        BiFunction<Integer, Integer, Integer> direction = clockwise ? Functions.getNextValueClockwise : Functions.getNextValueCounterClockwise;
        int next = direction.apply(seats.size() - 1, startSeatNumber);
        return getSeat(next);
    }

    protected Seat<T> getSeat(int seatNumber) {
        return seats.parallelStream().filter(seat -> seat.getNumber() == seatNumber).findAny().orElseThrow();
    }

    public List<T> getOrderedPlayersWithBet() {
        return getSeats().stream().filter(Seat::hasPlayerWithBet).sorted(Comparator.comparing(Seat::getNumber)).map(Seat::getPlayer).toList();
    }

    public boolean hasSeat(ICasinoPlayer p) {
        return p != null && seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
    }

    @Override
    public List<T> getPlayers() {
        return seats.stream().filter(Seat::hasPlayer).map(Seat::getPlayer).toList();
    }

    public boolean hasPlayers() {
        return getReservedSeatCount() != 0;
    }

    public List<Seat<T>> findInactivePlayerSeats() {
        return getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != PlayerStatus.ACTIVE).toList();
    }

    public void sanitizeSeats(List<PlayerStatus> playersStatuses) {
        verifyCallersLock();
        List<Seat<T>> seatsForCleanup = seats.stream().filter(Seat::hasPlayer).filter(seat -> playersStatuses.contains(seat.getPlayer().getStatus())).toList();
        seatsForCleanup.forEach(Seat::sanitize);
    }

    public void sanitizeSeat(int seatNumber) {
        verifyCallersLock();
        getSeat(seatNumber).sanitize();
    }

    public void verifyPlayerHasSeat(T player) {
        if (!hasSeat(player))
            throw new PlayerNotFoundException("Player not found from table:" + player, 0);
    }

    @Override
    public synchronized void onClose() {
        sanitizeAllSeats();
        super.onClose();
    }
}
