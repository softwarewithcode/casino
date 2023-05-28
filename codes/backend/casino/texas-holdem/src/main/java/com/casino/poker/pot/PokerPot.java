package com.casino.poker.pot;

import com.casino.poker.player.PokerPlayer;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@JsonIncludeProperties(value = {"amount", "amountWithTableChips"})
public class PokerPot implements Pot {
    private BigDecimal chipsOnTable;
    private BigDecimal amount;
    private BigDecimal rake;
    private UUID roundId;
    private UUID potId;
    private List<PokerPlayer> players;
    private List<PokerPlayer> winners;
    private Instant sealed = null;
    private Instant completed = null;

    private final ReentrantLock potLock;

    public PokerPot() {
        // For UI purposes
        this.amount = BigDecimal.ZERO;
        this.chipsOnTable = BigDecimal.ZERO;
        potLock = new ReentrantLock();
        players = new ArrayList<>();
    }

    public PokerPot(UUID roundId) {
        this();
        if (roundId == null)
            throw new IllegalArgumentException("Cannot create pot without roundId");
        this.roundId = roundId;
        this.potId = UUID.randomUUID();
        this.amount = BigDecimal.ZERO;
        this.roundId = roundId;
    }

    @Override
    public BigDecimal getRake() {
        return rake == null ? BigDecimal.ZERO : rake.setScale(2, RoundingMode.DOWN);
    }

    @Override
    public boolean isCompleted() {
        return completed != null;
    }

    @Override
    public void complete() {
        try {
            potLock.lock();
            verifyAccessControl();
            completed = Instant.now();
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public void clearTableChips() {
        try {
            potLock.lock();
            verifyAccessControl();
            chipsOnTable = BigDecimal.ZERO;
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public void deductRake(BigDecimal rakeAmount) {
        verifyAccessControl();
        BigDecimal temp = this.amount.subtract(rakeAmount);
        if (BigDecimal.ZERO.compareTo(temp) > 0)
            throw new IllegalArgumentException("Subtraction produced negative number, PotAmount:" + this.amount + " rake:" + rakeAmount + " =" + temp);
        this.amount = temp;
        this.rake = rakeAmount;
    }

    private void verifyAccessControl() {
        if (isCompleted())
            throw new IllegalStateException("Pot is completed");
    }

    @Override
    public BigDecimal getAmount() {
        return amount.setScale(2, RoundingMode.DOWN);
    }

    @Override
    public BigDecimal getAmountWithTableChips() { // For serialization also
        return amount.add(chipsOnTable).setScale(2, RoundingMode.DOWN);
    }

    @Override
    public List<PokerPlayer> getPlayers() {
        return players;
    }

    @Override
    public void seal() {
        try {
            potLock.lock();
            verifyAccessControl();
            sealed = Instant.now();
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public boolean isSealed() {
        return this.sealed != null;
    }

    @Override
    public void addTableChips(BigDecimal count) {
        try {
            potLock.lock();
            verifyAccessControl();
            this.chipsOnTable = this.chipsOnTable.add(count);
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public void add(BigDecimal additionalAmount) {
        try {
            potLock.lock();
            verifyAccessControl();
            this.amount = amount.add(additionalAmount);
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public void removePlayer(PokerPlayer player) {
        try {
            if (this.players == null)
                return;
            potLock.lock();
            verifyAccessControl();
            this.players = this.players.stream().filter(potPlayer -> !potPlayer.equals(player)).collect(Collectors.toList());
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public List<PokerPlayer> getWinners() {
        return winners;
    }

    @Override
    public void setWinners(List<PokerPlayer> potWinners) {
        try {
            potLock.lock();
            verifyAccessControl();
            this.winners = potWinners;
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public void setPlayers(List<PokerPlayer> players) {
        try {
            potLock.lock();
            verifyAccessControl();
            this.players = players;
        } finally {
            potLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "PokerPot{" + "amount=" + amount + ", rake=" + rake + ", roundId=" + roundId + ", potId=" + potId + ", players=" + players + ", winners=" + winners + ", sealed=" + sealed + '}';
    }
}
