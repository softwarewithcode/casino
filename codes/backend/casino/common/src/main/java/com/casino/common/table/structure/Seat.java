package com.casino.common.table.structure;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;

public class Seat<T extends CasinoPlayer> {
    private final int number;
    private T player;

    public Seat(int number) {
        this.number = number;
    }

    public Seat(int number, T player) {
        super();
        this.number = number;
        this.player = player;
    }

    public synchronized Optional<Seat<T>> take(T newPlayer) {
        if (player != null)
            return Optional.empty();
        player = newPlayer;
        return Optional.of(this);
    }

    public boolean hasPlayerWithBet() {
        return hasPlayer() && this.player.hasBet();
    }

    public boolean hasPlayer() {
        return this.player != null;
    }

    public boolean hasActivePlayer() {
        return hasPlayer() && player.getStatus() == PlayerStatus.ACTIVE;
    }

    public boolean hasNewPlayer() {
        return hasPlayer() && player.getStatus() == PlayerStatus.NEW;
    }

    public boolean hasPlayerWhoShouldStandUp() {
        return hasPlayer() && player.shouldStandUp();
    }

    public boolean hasActivePlayerCoveringAmount(BigDecimal requiredAmount) {
        return this.hasActivePlayer() && player.coversAmount(requiredAmount);
    }

    public boolean hasNewPlayerCoveringAmount(BigDecimal requiredAmount) {
        return this.hasNewPlayer() && player.coversAmount(requiredAmount);
    }

    public void sanitize() {
        player = null;
    }

    public boolean removePlayerIfHolder(CasinoPlayer player) {
        if (this.player == null || player == null)
            return false;
        if (this.player.equals(player)) {
            this.player = null;
            return true;
        }
        return false;
    }

    public T getPlayer() {
        return player;
    }

    public void setPlayer(T player) {
        this.player = player;
    }

    public Integer getNumber() {
        return number;
    }

    public boolean isAvailable() {
        return player == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Seat [number=" + number + ", player=" + player + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Seat other = (Seat) obj;
        return number == other.number;
    }

}
