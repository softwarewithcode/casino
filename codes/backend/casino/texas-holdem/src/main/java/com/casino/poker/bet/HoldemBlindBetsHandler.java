package com.casino.poker.bet;

import com.casino.common.player.PlayerStatus;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.PokerData;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.pot.PotHandler;
import com.casino.poker.round.PokerRound;
import com.casino.poker.table.PokerTable;

import java.util.List;

public class HoldemBlindBetsHandler implements BlindBetsHandler {

    private final PotHandler potHandler;

    public HoldemBlindBetsHandler(PotHandler potHandler) {
        this.potHandler = potHandler;
    }

    @Override
    public boolean isMissingAnyBlind(PokerPlayer player) {
        return player.getMissingBlindBetTokens().contains(BetToken.BIG_BLIND) || player.getMissingBlindBetTokens().contains(BetToken.SMALL_BLIND);
    }

    @Override
    public void postSmallBlind(PokerPlayer player) {
        PokerData initData = (PokerData) player.getTable().getDealer().getGameData();
        player.addChipsOnTable(initData.smallBlind());
        potHandler.addTableChipsCount(initData.smallBlind(), player);
    }

    @Override
    public void postBigBlind(PokerPlayer player) {
        PokerData initData = (PokerData) player.getTable().getDealer().getGameData();
        player.addChipsOnTable(initData.bigBlind());
        potHandler.addTableChipsCount(initData.bigBlind(), player);
    }

    @Override
    public boolean isMissingBothBlinds(PokerPlayer player) {
        return player.getMissingBlindBetTokens().contains(BetToken.BIG_BLIND) && player.getMissingBlindBetTokens().contains(BetToken.SMALL_BLIND);
    }

    @Override
    public void collectMissedBlinds(PokerTable<PokerPlayer> table) {
        PokerPlayer bigBlindPlayer = table.getRound().getBigBlindPlayer();
        List<PokerPlayer> missedBlindsPlayers = table.getRound().getActivePlayers().stream()
                .filter(this::isMissingAnyBlind)
                .filter(player -> !player.equals(bigBlindPlayer)).toList();
        if (isMissingAnyBlind(bigBlindPlayer))
            clearMissedBlinds(bigBlindPlayer);
        missedBlindsPlayers.forEach(this::payMissingBlindBets);
    }

    @Override
    public void collectBlinds(PokerTable<PokerPlayer> table) {
        collectSmallAndBigBlind(table);
        collectMissedBlinds(table);
        collectNewPlayerBlinds(table);
    }

    @Override
    public void handleBlindBets(PokerTable<PokerPlayer> table) {
        addMissingBlindsTokens(table);
        collectBlinds(table);
    }

    @Override
    public boolean isMissingBigBlind(PokerPlayer player) {
        return player.getMissingBlindBetTokens().contains(BetToken.BIG_BLIND);
    }

    @Override
    public boolean isMissingSmallBlind(PokerPlayer player) {
        return player.getMissingBlindBetTokens().contains(BetToken.SMALL_BLIND);
    }

    @Override
    public void addMissingBlindBetToken(PokerPlayer player, BetToken token) {
        player.addMissingBlindBetToken(token);
    }

    @Override
    public void clearMissedBlinds(PokerPlayer player) {
        player.clearMissedBlindBets();
    }

    private void addMissingBlindsTokens(PokerTable<PokerPlayer> table) {
        List<PokerPlayer> sitOutsBetweenButtonAndBigBlindPlayers = getPlayersInBetweenButtonAndBigBlindPlayer(List.of(PlayerStatus.SIT_OUT), table);
        PokerRound previousRound = table.getPreviousRound();
        if (previousRound == null)
            return;
        PokerPlayer previousBigBlindPlayer = previousRound.getBigBlindPlayer();
        if (sitOutsBetweenButtonAndBigBlindPlayers.contains(previousBigBlindPlayer)) {
            addMissingBlindBetToken(previousBigBlindPlayer, BetToken.SMALL_BLIND);
            sitOutsBetweenButtonAndBigBlindPlayers = removePlayer(sitOutsBetweenButtonAndBigBlindPlayers, previousBigBlindPlayer);
        }
        // Should be only one SIT_OUT player at max
        if (sitOutsBetweenButtonAndBigBlindPlayers.size() > 0) {
            if (!isMissingBigBlind(sitOutsBetweenButtonAndBigBlindPlayers.get(0)))
                addMissingBlindBetToken(sitOutsBetweenButtonAndBigBlindPlayers.get(0), BetToken.BIG_BLIND);
            else
                addMissingBlindBetToken(sitOutsBetweenButtonAndBigBlindPlayers.get(0), BetToken.SMALL_BLIND);
        }

        List<PokerPlayer> comebackPlayers = getPlayersInBetweenButtonAndBigBlindPlayer(List.of(PlayerStatus.ACTIVE), table);
        if (table.getRound().getSmallBlindPlayer() != null)
            comebackPlayers = removePlayer(comebackPlayers, table.getRound().getSmallBlindPlayer());
        comebackPlayers.forEach(comebackPlayer -> addMissingBlindBetToken(comebackPlayer, BetToken.SMALL_BLIND));
    }

    private List<PokerPlayer> removePlayer(List<PokerPlayer> players, PokerPlayer playerToBeRemoved) {
        return players.stream().filter(player -> !player.equals(playerToBeRemoved)).toList();
    }

    private List<PokerPlayer> getPlayersInBetweenButtonAndBigBlindPlayer(List<PlayerStatus> allowedStatuses, PokerTable<PokerPlayer> table) {
        PokerRound round = table.getRound();
        Integer buttonSeatNumber = round.getPositions().buttonSeatNumber();
        Integer bigBlindSeatNumber = round.getBigBlindPlayer().getSeatNumber();
        return HoldemFunctions.getPlayersSittingInBetweenSeatsWithStatus(buttonSeatNumber, bigBlindSeatNumber, table.getPlayers(), allowedStatuses);
    }

    private void payMissingBlindBets(PokerPlayer holdemPlayer) {
        PokerTable<PokerPlayer> table = (PokerTable) holdemPlayer.getTable();
        PokerData initData = (PokerData) table.getDealer().getGameData();
        if (isMissingSmallBlind(holdemPlayer)) {
            holdemPlayer.subtractFromBalance(initData.smallBlind());
            table.getDealer().getPotHandler().addToActivePot(initData.smallBlind());
        }
        if (isMissingBigBlind(holdemPlayer)) {
            potHandler.addTableChipsCount(initData.bigBlind(), holdemPlayer);
            holdemPlayer.addChipsOnTable(initData.bigBlind());
        }
        clearMissedBlinds(holdemPlayer);
    }


    private void collectSmallAndBigBlind(PokerTable<PokerPlayer> table) {
        PokerData initData = (PokerData) table.getDealer().getGameData();
        if (table.getRound().getPositions().sb() != null)
            postSmallBlind(table.getRound().getPositions().sb());
        postBigBlind(table.getRound().getPositions().bb());
        table.getRound().setInitialRaiseAmount(initData.bigBlind().subtract(initData.smallBlind()));
    }

    private void collectNewPlayerBlinds(PokerTable<PokerPlayer> table) {
        if (table.getRounds().size() == 1)
            return;// No new player's blind in the first round since button position is random
        PokerData initData = (PokerData) table.getDealer().getGameData();
        List<PokerPlayer> newPlayers = table.getRound().newPlayers();
        List<PokerPlayer> playersMissingNewPlayerBlind = table.getRound().getPlayers().stream().filter(newPlayers::contains).filter(player -> !player.equals(table.getRound().getBigBlindPlayer()))
                .filter(player -> !player.equals(table.getRound().getSmallBlindPlayer())).filter(player -> !player.equals(table.getRound().getPositions().buttonPlayer())).toList();
        playersMissingNewPlayerBlind.forEach(player -> player.addChipsOnTable(initData.bigBlind()));
    }

}
