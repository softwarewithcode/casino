package com.casino.poker.round;

import com.casino.common.cards.Card;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.positions.HoldemRoundPlayers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public final class PokerRound {
    private final HoldemRoundPlayers positions;
   // private final List<PokerPlayer> players;
    private final List<PokerPlayer> newPlayers;
    private BigDecimal initialRaiseAmount;
    private PokerPlayer lastRaiser;
    private PokerPlayer lastSpeakingPerson;
    private List<Card> tableCards;
    private final UUID roundId;
    private Instant completed;

    public PokerRound(HoldemRoundPlayers positions, List<PokerPlayer> newPlayers, UUID tableId) {
        this.positions = positions;
        roundId = UUID.randomUUID();
        tableCards = new ArrayList<>(5);
        setLastSpeakingPlayer(positions.bb());
        initialRaiseAmount = BigDecimal.ZERO;
      //  players = previousRoundPlayers;
        this.newPlayers = newPlayers;
    }

    public void addCard(Card c) {
        tableCards.add(c);
    }

    public void setCards(List<Card> cards) {
        this.tableCards = cards;
    }

    public void setLastSpeakingPlayer(PokerPlayer lastSpeakingPerson) {
        this.lastSpeakingPerson = lastSpeakingPerson;
    }

    public PokerPlayer getLastSpeakingPlayer() {
        return lastSpeakingPerson;
    }

    public List<Card> getTableCards() {
        return tableCards.stream().toList();
    }
    public void clearTableCards() {
    	tableCards.clear();
    }
    public PokerPlayer getSmallBlindPlayer() {
        return positions.sb();
    }

    public List<PokerPlayer> getActivePlayers() {
        return this.getPlayers().stream().filter(PokerPlayer::isActive).toList();
    }

    public PokerPlayer getBigBlindPlayer() {
        return positions.bb();
    }

    public HoldemRoundPlayers getPositions() {
        return positions;
    }


    public void complete() {
        this.completed = Instant.now();
    }

    public UUID getRoundId() {
        return roundId;
    }

    public Instant getCompleted() {
        return completed;
    }

    public PokerPlayer getLastRaiser() {
        return lastRaiser;
    }


    public void setLastRaiserQQQ(PokerPlayer lastRaiser) {
        this.lastRaiser = lastRaiser;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public BigDecimal getInitialRaiseAmount() {
        return initialRaiseAmount != null ? initialRaiseAmount.setScale(2, RoundingMode.DOWN) : BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
    }

    public void setInitialRaiseAmount(BigDecimal initialRaiseAmount) {
        this.initialRaiseAmount = initialRaiseAmount;
    }

    public List<PokerPlayer> getPlayers() {
        return positions.players();
    }

    public BigDecimal getMostChipsOnTable() {
        return getPlayers().stream().max(Comparator.comparing(PokerPlayer::getTableChipCount)).map(PokerPlayer::getTableChipCount).orElse(BigDecimal.ZERO);
    }

    public boolean isCompleted() {
        return completed != null;
    }

    public boolean isHeadsUp() {
        return getPlayers().size() == 2;
    }

    public boolean isPlayer(PokerPlayer player) {
        if (player == null)
            return false;
        return getPlayers().contains(player);
    }

    public boolean isWinnerKnown() {
        return getPlayers().stream().filter(PokerPlayer::hasHoleCards).toList().size() == 1;
    }

    public List<PokerPlayer> newPlayers() {
        return newPlayers;
    }
}
