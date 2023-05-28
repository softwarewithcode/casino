package com.casino.poker.round.positions;

import com.casino.common.player.PlayerStatus;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.table.HoldemTable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class PokerPositionsBuilder {

    private PokerPlayer bigBlindPlayer;
    private PokerPlayer smallBlindPlayer;
    private PokerPlayer previousBigBlindPlayer;
    private PokerPlayer previousSmallBlindPlayer;
    private PokerPlayer buttonPlayer;
    private Integer buttonSeatNumber;
    private Integer smallBlindSeatNumber;
    private List<PokerPlayer> newPlayers;
    private final HoldemTable table;
    private List<PokerPlayer> roundCandidates;
    private Integer predefinedButtonPosition;

    public static final String BUTTON_POSITION_IN_TEST = "TEST_RUNNER";
    private static final boolean TEST_RUNNER;
    private static final List<PlayerStatus> ACTIVE_AND_NEW_STATUSES_PLAYERS = List.of(PlayerStatus.NEW, PlayerStatus.SIT_OUT_AS_NEW, PlayerStatus.ACTIVE);
    private static final List<PlayerStatus> ACTIVE_PLAYERS = List.of(PlayerStatus.ACTIVE);

    static {
        String testMode = (String) System.getProperties().get(BUTTON_POSITION_IN_TEST);
        TEST_RUNNER = testMode != null && !testMode.equals("-1");
    }

    private void handleButtonPosition() {
        if (TEST_RUNNER) {
            String testMode = (String) System.getProperties().get(BUTTON_POSITION_IN_TEST);
            predefinedButtonPosition = Integer.parseInt(testMode);
        }
    }

    private PokerPositionsBuilder(HoldemTable table, List<PokerPlayer> roundAttendants) {
        this.table = table;
        this.newPlayers = roundAttendants.stream().filter(PokerPlayer::isNew).toList();
        this.roundCandidates = List.copyOf(roundAttendants);
        handleButtonPosition();
        organize();
    }

    public PokerRoundPlayers build() {
        return new PokerRoundPlayers(smallBlindPlayer, bigBlindPlayer, buttonPlayer, buttonSeatNumber, smallBlindSeatNumber, bigBlindPlayer.getSeatNumber(), roundCandidates);
    }

    public static PokerPositionsBuilder of(HoldemTable table, List<PokerPlayer> roundAttendants) {
        return new PokerPositionsBuilder(table, roundAttendants);
    }

    private void organize() {
        if (shouldInitFirstRound()) {
            initFirstRound();
        } else
            initContinuationRound();
    }

    private void initContinuationRound() {
        previousBigBlindPlayer = table.getRound().getBigBlindPlayer();
        previousSmallBlindPlayer = table.getRound().getSmallBlindPlayer();
        List<PokerPlayer> filteredCandidates = dropNewCandidatesSittingBetweenButtonAndBigBlind();
        roundCandidates = collectRoundCandidates(filteredCandidates);
        assignContinuationRoundButton();
        calculateSmallBlindPlayer();
        calculateBigBlindPlayer();
        bigBlindPlayer.setWaitBigBlind(false);
        removePlayersWaitingForBigBlind();
        roundCandidates.forEach(candidate -> candidate.setStatus(PlayerStatus.ACTIVE));
        removePlayersBetweenButtonAndBigBlindPlayerExceptSmallBlindPlayer();
        table.getButton().moveButton(buttonSeatNumber);
    }

    private void removePlayersWaitingForBigBlind() {
        roundCandidates = roundCandidates.stream().filter(candidate -> !candidate.isWaitingBigBlind()).toList();
    }

    private void removePlayersBetweenButtonAndBigBlindPlayerExceptSmallBlindPlayer() {
        List<PokerPlayer> playersSittingInBetweenButtonAndBigBlind = HoldemFunctions.getPlayersSittingInBetweenSeatsWithStatus(buttonSeatNumber, bigBlindPlayer.getSeatNumber(), roundCandidates, ACTIVE_PLAYERS);
        roundCandidates = roundCandidates.stream().filter(player -> !playersSittingInBetweenButtonAndBigBlind.contains(player) || isSmallBlindPlayer(player)).toList();
    }

    private boolean isSmallBlindPlayer(PokerPlayer player) {
        return player.equals(smallBlindPlayer);
    }

    private List<PokerPlayer> collectRoundCandidates(List<PokerPlayer> filteredCandidates) {
        return roundCandidates.stream().filter(candidate -> candidate.getStatus() == PlayerStatus.ACTIVE || filteredCandidates.contains(candidate)).toList();
    }

    private void calculateBigBlindPlayer() {
        if (roundCandidates.size() == 2)
            bigBlindPlayer = roundCandidates.stream().filter(player -> !player.equals(smallBlindPlayer)).findFirst().orElseThrow();
        else if (smallBlindPlayer != null) {
            List<PokerPlayer> ordered = HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(smallBlindSeatNumber, roundCandidates, PokerPositionsBuilder.ACTIVE_AND_NEW_STATUSES_PLAYERS);
            bigBlindPlayer = ordered.stream().filter(candidate -> !candidate.equals(smallBlindPlayer)).findFirst().orElseThrow();
        } else
            bigBlindPlayer = HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(buttonSeatNumber, roundCandidates, PokerPositionsBuilder.ACTIVE_AND_NEW_STATUSES_PLAYERS).stream().filter(this::isValidBigBlindPlayerCandidate).findFirst()
                    .orElseThrow();
    }

    private boolean isValidBigBlindPlayerCandidate(PokerPlayer candidate) {
        return !candidate.equals(previousSmallBlindPlayer) && !candidate.getSeatNumber().equals(buttonSeatNumber);
    }

    private List<PokerPlayer> dropNewCandidatesSittingBetweenButtonAndBigBlind() {
        List<PokerPlayer> activeAndNewPlayers = getActiveAndNewPlayersStartingFromButtonPlayer();
        if (activeAndNewPlayers.size() == 0)
            return activeAndNewPlayers;
        activeAndNewPlayers.remove(0);//ButtonPlayer removed
        List<PokerPlayer> droppedPlayers = activeAndNewPlayers.stream().takeWhile(this::isNewAndNotBigBlind).toList();
        return activeAndNewPlayers.stream().filter(candidate -> !droppedPlayers.contains(candidate)).toList();
    }

    private List<PokerPlayer> getActiveAndNewPlayersStartingFromButtonPlayer() {
        return HoldemFunctions.getPlayersWithStatusInOrderByStartingSeatNumber(table.getRound().getPositions().buttonSeatNumber(), roundCandidates, ACTIVE_AND_NEW_STATUSES_PLAYERS);
    }

    private boolean isNewAndNotBigBlind(PokerPlayer candidate) {
        return candidate.getStatus().isNewStatus() && !candidate.equals(previousBigBlindPlayer);
    }

    private boolean shouldInitFirstRound() {
        return table.getRound() == null || table.getActivePlayerCount() < 2;
    }

    private void calculateSmallBlindPlayer() {
        findSmallBlindPlayerAmongstActivePlayers().ifPresent(this::assignSmallBlindPlayer);
    }

    private void assignSmallBlindPlayer(PokerPlayer player) {
        this.smallBlindPlayer = player;
        this.smallBlindSeatNumber = player.getSeatNumber();
    }

    private void initFirstRound() {
        activateNewPlayers();
        setSitOutPlayersStatusesToSitOutAsNew(); // Basically new game starts, as some/all actives are sitting out
        randomizeButtonPosition();
        buttonPlayer = table.getPlayer(table.getButton().getSeatNumber());
        buttonSeatNumber = buttonPlayer.getSeatNumber();
        if (roundCandidates.size() == 2) {
            smallBlindPlayer = buttonPlayer;
            smallBlindSeatNumber = smallBlindPlayer.getSeatNumber();
            bigBlindPlayer = findCandidateStartingFromSeatNumber(smallBlindPlayer.getSeatNumber());
        } else {
            smallBlindPlayer = findCandidateStartingFromSeatNumber(buttonPlayer.getSeatNumber());
            if (smallBlindPlayer != null)
                smallBlindSeatNumber = smallBlindPlayer.getSeatNumber();
            assert smallBlindPlayer != null;
            bigBlindPlayer = findCandidateStartingFromSeatNumber(smallBlindPlayer.getSeatNumber());
        }
        roundCandidates.forEach(player -> player.setWaitBigBlind(false));
    }

    private void setSitOutPlayersStatusesToSitOutAsNew() {
        List<PokerPlayer> sitOutPlayers = table.getPlayers().stream().filter(PokerPlayer::isSitOut).toList();
        sitOutPlayers.forEach(player -> player.setStatus(PlayerStatus.SIT_OUT_AS_NEW));
    }

    private void activateNewPlayers() {
        newPlayers = roundCandidates.stream().filter(PokerPlayer::isNew).toList();
        newPlayers.forEach(player -> player.setStatus(PlayerStatus.ACTIVE));
    }

    private Optional<PokerPlayer> findSmallBlindPlayerAmongstActivePlayers() {
        if (roundCandidates.size() == 2)
            return Optional.of(table.getPlayer(buttonSeatNumber));
        return roundCandidates.stream().filter(candidate -> candidate.getSeatNumber().equals(previousBigBlindPlayer.getSeatNumber())).findFirst();
    }

    private void assignContinuationRoundButton() { // Should take into account leaving players, new players, players who switch
        // seats and player's active status
        if (shouldKeepButtonPosition())
            buttonSeatNumber = table.getRound().getPositions().buttonSeatNumber();
        else
            buttonSeatNumber = calculateMovingButtonPosition();
        PokerPlayer buttonCandidate = table.getPlayer(buttonSeatNumber);
        if (buttonCandidate != null && buttonCandidate.isActive())
            buttonPlayer = buttonCandidate;
    }

    private Integer calculateMovingButtonPosition() {
        if (previousRoundWasHeadsUp())
            return previousBigBlindPlayer.getSeatNumber();
        if (hasShrinkToHeadsUpGameBetweenPreviousRoundPlayers()) {
            return table.getDealer().getNextPlayerWithStatus(table.getRound().getPositions().buttonPlayer(), List.of(PlayerStatus.ACTIVE)).orElseThrow().getSeatNumber();
        }
        if (table.getRound().getPositions().sbSeatNumber() == null) {
            return table.getRound().getPositions().buttonSeatNumber();
        }
        return table.getRound().getPositions().sbSeatNumber();
    }

    private boolean shouldKeepButtonPosition() {
        if (previousSmallBlindPlayer == null)
            return false;
        Optional<PokerPlayer> nextFromPreviousSmallBlindPlayer = table.getDealer().getNextPlayerWithStatus(previousSmallBlindPlayer, List.of(PlayerStatus.ACTIVE));
        if (previousRoundWasHeadsUp()) {
            if (nextFromPreviousSmallBlindPlayer.isEmpty())
                return true;
            PokerPlayer nextPlayer = nextFromPreviousSmallBlindPlayer.get();
            if (nextPlayer.equals(previousBigBlindPlayer) && roundCandidates.size() == 2)
                return false;
            return nextPlayer.equals(previousBigBlindPlayer) && roundCandidates.size() > 2;
        }

        if (isPreviousSmallBlindPlayerContinuing())
            return false;
        else if (hasShrinkToHeadsUpGameBetweenPreviousRoundPlayers()) {
            return previousSmallBlindPlayer != null && !previousSmallBlindPlayer.equals(nextFromPreviousSmallBlindPlayer.get()) && !previousBigBlindPlayer.equals(nextFromPreviousSmallBlindPlayer.get());
        }
        return true;
    }

    private boolean previousRoundWasHeadsUp() {
        return table.getRound().isHeadsUp();
    }

    private boolean hasShrinkToHeadsUpGameBetweenPreviousRoundPlayers() {
        boolean previousRoundWasMultiplayer = (table.getRound().getPlayers().size()) > 2;
        return previousRoundWasMultiplayer && roundCandidates.stream().filter(PokerPlayer::isActive).toList().size() == 2;
    }

    private boolean isPreviousSmallBlindPlayerContinuing() {
        return previousSmallBlindPlayer != null && previousSmallBlindPlayer.isActive();
    }

    private void randomizeButtonPosition() {
        int nth = ThreadLocalRandom.current().nextInt(0, roundCandidates.size());
        Integer buttonPosition = roundCandidates.get(nth).getSeatNumber();
        if (predefinedButtonPosition != null)
            buttonPosition = predefinedButtonPosition;
        table.getButton().moveButton(buttonPosition);
    }

    public PokerPlayer findCandidateStartingFromSeatNumber(int fromSeatNumber) {
        int highestReservedSeatNumber = roundCandidates.stream().map(PokerPlayer::getSeatNumber).max(Comparator.naturalOrder()).orElseThrow(IllegalArgumentException::new);
        if (highestReservedSeatNumber == fromSeatNumber)
            return roundCandidates.get(0);
        return roundCandidates.stream().filter(player -> player.getSeatNumber() > fromSeatNumber).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
