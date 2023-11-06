package com.casino.poker.actions;

import com.casino.common.bet.BetRange;
import com.casino.common.functions.Functions;
import com.casino.poker.dealer.PokerDealer;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.table.HoldemTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PokerActionCreator implements ActionCreator {
    @Override
    public List<PokerAction> createActions(HoldemPlayer player) {
        List<PokerAction> actions = new ArrayList<>();
        if (player.isAllIn())
            return actions;
        actions.add(PokerAction.of(null, PokerActionType.FOLD));
        actions.add(PokerAction.of(new BetRange(player.getCurrentBalance(), player.getCurrentBalance()), PokerActionType.ALL_IN));
        HoldemTable table = player.getTable();
        BigDecimal mostChipsOnTable = table.getRound().getMostChipsOnTable();
        if (canCheck(player, mostChipsOnTable))
            actions.add(PokerAction.of(null, PokerActionType.CHECK));
        if (canBet(player, mostChipsOnTable))
            actions.add(createBetAction(player));
        if (canRaise(player, mostChipsOnTable))
            actions.add(createRaiseAction(player));
        if (canCall(player, mostChipsOnTable))
            actions.add(createCallAction(player, mostChipsOnTable));
        return actions;
    }

    private PokerAction createCallAction(HoldemPlayer player, BigDecimal biggestMoneyAmountOnTable) {
        BigDecimal callAmount = calculateCallAmount(player, biggestMoneyAmountOnTable);
        BetRange range = new BetRange(callAmount, callAmount);
        return PokerAction.of(range, PokerActionType.CALL);
    }

    private PokerAction createRaiseAction(HoldemPlayer player) {
        BigDecimal minRaise = ((PokerDealer) player.getTable().getDealer()).calculateMinRaise();
        BetRange range = new BetRange(minRaise, player.getCurrentBalance().add(player.getTableChipCount()));
        return PokerAction.of(range, PokerActionType.BET_RAISE);
    }

    private PokerAction createBetAction(HoldemPlayer player) {
        BigDecimal minBet = player.getTable().getDealer().getGameData().bigBlind();
        BetRange range = new BetRange(minBet, player.getCurrentBalance());
        return PokerAction.of(range, PokerActionType.BET_RAISE);
    }

    private boolean canCall(HoldemPlayer player, BigDecimal mostChipsOnTable) {
        BigDecimal chipsOnTable = player.getTableChipCount();
        HoldemTable table = player.getTable();
        if (!HoldemFunctions.hasAnybodyMoreChipsOnTable(chipsOnTable, table))
            return false;
        BigDecimal callAmount = calculateCallAmount(player, mostChipsOnTable);
        return player.coversAmount(callAmount);
    }

    private BigDecimal calculateCallAmount(HoldemPlayer player, BigDecimal biggestMoneyAmountOnTable) {
        return biggestMoneyAmountOnTable.subtract(player.getTableChipCount());
    }

    private boolean canBet(HoldemPlayer player, BigDecimal biggestMoneyAmountOnTable) {
        if (Functions.isFirstMoreThanSecond.apply(biggestMoneyAmountOnTable, BigDecimal.ZERO))
            return false;
        return player.coversAmount(player.getTable().getDealer().getGameData().getMinBet());
    }

    private boolean canCheck(HoldemPlayer player, BigDecimal biggestMoneyAmountOnTable) {
        return player.getTableChipCount().compareTo(biggestMoneyAmountOnTable) == 0;
    }

    private boolean canRaise(HoldemPlayer player, BigDecimal biggestAmountMoneyOnTable) {
        if (!((PokerDealer) player.getTable().getDealer()).isAnyChipsOnTable())
            return false;
        if (player.hasMostChipsOnTable(biggestAmountMoneyOnTable))
            return false;
        BigDecimal minRaise = ((PokerDealer) player.getTable().getDealer()).calculateMinRaise();
        return Functions.isFirstMoreThanSecond.apply(player.getCurrentBalance(), minRaise); // If amounts are equal then it's all in
    }

}
