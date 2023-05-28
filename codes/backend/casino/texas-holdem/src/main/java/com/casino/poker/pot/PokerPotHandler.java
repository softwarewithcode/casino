package com.casino.poker.pot;

import com.casino.common.functions.Functions;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.table.PokerTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PokerPotHandler implements PotHandler {
    private final PokerTable<PokerPlayer> table;
    private final BigDecimal rakePercent;
    private final BigDecimal rakeCap;
    private List<Pot> pots;

    public PokerPotHandler(PokerTable<PokerPlayer> table, BigDecimal rakePercent, BigDecimal rakeCap) {
        pots = new ArrayList<>();
        this.table = table;
        this.rakePercent = rakePercent;
        this.rakeCap = rakeCap;
    }

    @Override
    public void clearPots() {
        this.pots = new ArrayList<>();
    }

    @Override
    public List<Pot> getPots() {
        return pots.stream().toList();
    }

    @Override
    public List<Pot> completePots() {
        for (var pot : pots) {
            completePot(pot);
        }
        return pots.stream().toList();
    }

    private BigDecimal calculateRake(Pot pot) {
        BigDecimal collectedTotalRakeFromAllPots = pots.stream().map(Pot::getRake).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!shouldCollectMoreRake(collectedTotalRakeFromAllPots))
            return BigDecimal.ZERO;
        BigDecimal potRake = pot.getAmount().multiply(rakePercent);
        BigDecimal potentialRake = collectedTotalRakeFromAllPots.add(potRake);
        if (Functions.isFirstMoreOrEqualToSecond.apply(rakeCap, potentialRake))
            return potRake;
        return rakeCap.subtract(collectedTotalRakeFromAllPots);
    }

    private void completePot(Pot pot) {
        List<PokerPlayer> potWinners = new ArrayList<>();
        if (table.getGamePhase() != HoldemPhase.PRE_FLOP)
            pot.deductRake(calculateRake(pot));
        List<PokerPlayer> playersByHandValue = getPlayersByHandValue(pot);
        for (PokerPlayer player : playersByHandValue) {
            if (isWinner(potWinners, player))
                potWinners.add(player);
        }
        pot.setWinners(potWinners);
        pot.complete();
    }

    private boolean isWinner(List<PokerPlayer> potWinners, PokerPlayer player) {
        return potWinners.isEmpty() || potWinners.get(0).getHand().compareTo(player.getHand()) == 0;
    }

    private List<PokerPlayer> getPlayersByHandValue(Pot pot) {
        if (pot.getPlayers().size() == 0)
            return Collections.emptyList();
        if (table.getRound().isWinnerKnown())
            return pot.getPlayers().stream().filter(PokerPlayer::hasHoleCards).toList();
        return pot.getPlayers().stream().sorted(Comparator.comparing(PokerPlayer::getHand)).toList();
    }

    private boolean shouldCollectMoreRake(BigDecimal collectedRake) {
        return Functions.isFirstMoreThanSecond.apply(rakeCap, collectedRake);
    }

    @Override
    public BigDecimal getActivePotAmount() {
        return getActivePot().getAmount();
    }

    @Override
    public BigDecimal getActivePotAmountWithTableChips() {
        return getActivePot().getAmountWithTableChips();
    }

    @Override
    public void addToActivePot(BigDecimal amount) {
        getActivePot().add(amount);
    }

    private Pot getActivePot() {
        if (pots.isEmpty()) {
            pots.add(new PokerPot(table.getRound().getRoundId()));
        }
        return pots.get(pots.size() - 1);
    }

    @Override
    public void removePlayer(PokerPlayer player) {
        pots.forEach(pot -> pot.removePlayer(player));
    }

    @Override
    public void addTableChipsCount(BigDecimal amount, PokerPlayer player) {
        getActivePot().addTableChips(amount);
        if (!getActivePot().getPlayers().contains(player))
            getActivePot().getPlayers().add(player);
    }

    @Override
    public void onPhaseCompletion() {
        while (shouldAddToPot()) {
            if (shouldActivateNewSidePot())
                activateNewSidePot();
            BigDecimal amount = calculateChipsExtractionAmount();
            extractPlayerChipsFromTableIntoPot(amount);
            if (shouldSealActivePot())
                getActivePot().seal();
        }
        getActivePot().clearTableChips();
    }

    private boolean shouldAddToPot() {
        return table.getRound().getPlayers().stream().filter(PokerPlayer::hasChipsOnTable).count() > 1;
    }

    private void activateNewSidePot() {
        pots.add(createEmptySidePotForPlayersWhoCanAct());
    }

    private Pot createEmptySidePotForPlayersWhoCanAct() {
        PokerPot pot = new PokerPot(table.getId());
        List<PokerPlayer> sidePotPlayers = getActivePot().getPlayers().stream().filter(player -> !player.isAllIn()).toList();
        pot.setPlayers(sidePotPlayers);
        return pot;
    }

    private boolean shouldSealActivePot() {
        return getActivePot().getPlayers().stream().filter(PokerPlayer::isAllIn).toList().size() > 0;
    }

    private void extractPlayerChipsFromTableIntoPot(BigDecimal extractionAmount) {
        List<PokerPlayer> playersWithChipsOnTable = getPlayersWithChipsOnTable();
        getPlayersWithChipsOnTable().forEach(player -> player.removeSomeChipsFromTable(extractionAmount));
        Pot activePot = getActivePot();
        BigDecimal additionalAmount = extractionAmount.multiply(BigDecimal.valueOf(playersWithChipsOnTable.size()));
        activePot.add(additionalAmount);
        activePot.setPlayers(playersWithChipsOnTable);
    }

    private BigDecimal calculateChipsExtractionAmount() {
        List<PokerPlayer> playersWithChipsOnTable = getPlayersWithChipsOnTable();
        return Collections.min(playersWithChipsOnTable, Comparator.comparing(PokerPlayer::getTableChipCount)).getTableChipCount();
    }

    private List<PokerPlayer> getPlayersWithChipsOnTable() {
        return table.getRound().getPlayers().stream().filter(PokerPlayer::hasChipsOnTable).collect(Collectors.toList());
    }

    private boolean shouldActivateNewSidePot() {
        return getActivePot().isSealed();
    }
}
