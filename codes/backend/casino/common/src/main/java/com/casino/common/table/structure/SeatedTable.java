package com.casino.common.table.structure;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.IllegalPlayerCountException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.functions.Functions;
import com.casino.common.game.GameData;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.user.Connectable;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIncludeProperties(value = { /* implementing subclass defines exact fields */})
public abstract class SeatedTable<T extends CasinoPlayer> extends Table implements ISeatedTable<T> {
    private static final Logger LOGGER = Logger.getLogger(SeatedTable.class.getName());
    private final Set<Seat<T>> seats;
    protected TableCard<? extends GameData> tableCard;

    protected SeatedTable(TableData tabledata) {
        super(tabledata);
        Set<Seat<T>> seats = IntStream.range(0, tabledata.thresholds().seatCount()).mapToObj(i -> new Seat<T>(i)).collect(Collectors.toSet());
        this.seats = Collections.synchronizedSet(seats);
    }

    protected Optional<Seat<T>> join(String seatNumber, T player) {
        Integer seatNumbr = seatNumber != null ? Integer.parseInt(seatNumber) : null;
        Optional<Seat<T>> seatOptional = trySeat(seatNumbr, player);
        if (seatOptional.isPresent())
            super.removeWatcher(player.getId());
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
            seat = seats.stream().filter(Seat::isAvailable).findAny().orElseThrow(IllegalPlayerCountException::new);
        else if (seatNumber < 0 || seatNumber >= seats.size())
            throw new IllegalArgumentException("seat number is incorrect " + seatNumber + " table total:" + getSeats().size());
        else
            seat = seats.stream().filter(s -> Objects.equals(s.getNumber(), seatNumber)).findAny().orElseThrow(IllegalArgumentException::new);
        return seat.take(player);
    }
    @Override
    public boolean isStatusAllowingPlayerEntries() {
        return getStatus() == TableStatus.WAITING_PLAYERS || getStatus() == TableStatus.RUNNING;
    }
    private boolean isReservedSinglePlayerTable() {
        return getType() == TableType.SINGLEPLAYER && getPlayers().size() != 0;
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

    @Override
    public T getPlayer(UUID playerId) { // TO optional
        if (playerId == null)
            return null;
        Optional<Seat<T>> optionalSeat = getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getId().equals(playerId)).findAny();
        return optionalSeat.map(Seat::getPlayer).orElse(null);
    }

    @Override
    public Set<Seat<T>> getSeats() {
        return seats;
    }

    @Override
    public Seat<T> getNextSeat(int startSeatNumber, boolean clockwise) {
        BiFunction<Integer, Integer, Integer> direction = clockwise ? Functions.getNextValueClockwise : Functions.getNextValueCounterClockwise;
        int next = direction.apply(seats.size() - 1, startSeatNumber);
        return getSeat(next);
    }

    protected Seat<T> getSeat(int seatNumber) {
        return seats.parallelStream().filter(seat -> seat.getNumber() == seatNumber).findAny().orElseThrow();
    }

    @Override
    public List<T> getOrderedPlayersWithBet() {
        return getSeats().stream().filter(Seat::hasPlayerWithBet).sorted(Comparator.comparing(Seat::getNumber)).map(Seat::getPlayer).toList();
    }

    private boolean hasSeat(CasinoPlayer p) {
        return p != null && seats.stream().anyMatch(seat -> p.equals(seat.getPlayer()));
    }

    @Override
    public List<T> getPlayers() {
        return seats.stream().filter(Seat::hasPlayer).map(Seat::getPlayer).toList();
    }

    @Override
    public boolean hasPlayers() {
        return getReservedSeatCount() != 0;
    }

    @Override
    public List<Seat<T>> findInactivePlayerSeats() {
        return getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != PlayerStatus.ACTIVE).toList();
    }

    @Override
    public void sanitizeSeatsByPlayerStatus(List<PlayerStatus> playersStatuses) {
        verifyCallersLock();
        List<Seat<T>> seatsForCleanup = seats.stream().filter(Seat::hasPlayer).filter(seat -> playersStatuses.contains(seat.getPlayer().getStatus())).toList();
        seatsForCleanup.forEach(Seat::sanitize);
    }

    @Override
	public List<T> getPlayersWithStatus(List<PlayerStatus> anyOfStatuses) {
		return seats.stream().filter(Seat::hasPlayer).map(seat->seat.getPlayer()).filter(player -> anyOfStatuses.contains(player.getStatus())).toList();
	}

    @Override
    public void sanitizeSeat(int seatNumber) {
        verifyCallersLock();
        getSeat(seatNumber).sanitize();
    }

    @Override
    public synchronized void onClose() {
        setStatus(TableStatus.CLOSING);
        seats.forEach(Seat::sanitize);
        super.onClose();
    }

    @Override
    public void verifyPlayerHasSeat(T player) {
        if (!hasSeat(player))
            throw new PlayerNotFoundException("Player not found from table:" + player, 0);
    }

    @Override
    public void watch(Connectable watcher) {
        LOGGER.entering(getClass().getName(), "watch", getId());
        if (isPlaying(watcher)) {
            LOGGER.fine("User " + watcher.getId() + " is already playing in table:" + this);
            return;
        }
        if (super.joinAsWatcher(watcher)) {
            getDealer().onWatcherArrival(watcher);
        }
        LOGGER.exiting(getClass().getName(), "watch" + " player:" + getId() + " table:" + getId());
    }

    private boolean isPlaying(Connectable user) {
        return getPlayer(user.getId()) != null;
    }

    // Provides common implementation for watchers and players to leave from table.
    @Override
    public void leave(UUID playerOrWatcherId) {
        try {
            var player = getPlayer(playerOrWatcherId);
            if (player == null) {
                removeWatcher(playerOrWatcherId);
                return;
            }
            getLock().lock();// Only leaving players should lock the table. Leaving players affects turn based games..

            getDealer().onPlayerLeave(player);
        } finally {
            if (getLock().isHeldByCurrentThread())
                getLock().unlock();
        }
    }

    @Override
    public void refresh(UUID playerOrWatcherId) {
        var connectable = getPlayer(playerOrWatcherId);
        if (connectable != null)
            getDealer().refresh(connectable);
        else
            super.getWatcher(playerOrWatcherId).ifPresentOrElse(watcher -> getDealer().refresh(watcher), IllegalArgumentException::new);
    }

    @Override
    public TableCard<? extends GameData> getTableCard() { // For serialization
        List<Integer> seats = getSeats().stream().filter(seat -> !seat.hasPlayer()).map(Seat::getNumber).toList();
        tableCard.setAvailablePositions(seats);
        return tableCard;
    }

}
