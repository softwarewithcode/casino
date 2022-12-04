package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetPhaseClockTask;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Seat;
import com.casino.common.table.phase.GamePhase;

public class BlackjackDealer implements IDealer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());
	private final BetThresholds betThresholds;
	private final BlackjackTable table;
	private List<Card> decks; // 6 decks
	private IHand hand;

	public BlackjackDealer(BlackjackTable blackjackTable, BetThresholds betThresholds) {
		this.table = blackjackTable;
		this.betThresholds = betThresholds;
		this.decks = Deck.combineDecks(6);
		this.hand = new BlackjackDealerHand(UUID.randomUUID(), true);
	}

	private void startBetPhase() {
		table.updateGamePhase(GamePhase.BET);
		if (table.getActivePlayerCount() > 0) {
			BetPhaseClockTask task = new BetPhaseClockTask(table);
			this.getTable().getClock().startClock(task, 1000);
		}
	}

	public void handlePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.getPlayer() != null && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateTotalBet(bet, table);
		}, () -> {
			throw new PlayerNotFoundException("Player not found in table:" + table + " player:" + tablePlayer, 1);
		});
	}

	public IHand getHand() {
		return hand;
	}

	public void addCard(Card card) {
		this.hand.addCard(card);
		if (hand.isCompleteable())
			hand.complete();
	}

	public void initTable() {
		createDecks();
	}

	private void createDecks() {
		decks = Deck.combineDecks(6);
	}

	public List<Card> getDecks() {
		return decks;
	}

	public BetThresholds getBetThresholds() {
		return betThresholds;
	}

	public BlackjackTable getTable() {
		return table;
	}

	public boolean dealInitialCards() {
		if (!isAllowedToDealStartingHand())
			return false;
		IntStream.range(0, 2).forEach(i -> getPlayersWithBet().forEach(player -> dealCard(player.getHands().get(0), decks.remove(decks.size() - 1))));
		getPlayersWithBet().forEach(player -> completeActiveHandIfPossible(player));
		return true;
	}

	private void dealCard(IHand hand, Card card) {
		hand.addCard(card);
	}

	public void handleAdditionalCard(ICasinoPlayer player) {
		Card card = decks.remove(decks.size() - 1);
		IHand activeHand = getActiveHand(player);
		activeHand.addCard(card);
		completeActiveHandIfPossible(player);
	}

	private void completeActiveHandIfPossible(ICasinoPlayer player) {
		IHand activeHand = getActiveHand(player);
		if (activeHand != null && activeHand.isCompleteable()) {
			activeHand.complete();
		}
	}

	private IHand getActiveHand(ICasinoPlayer player) {
		return player.getHands().stream().filter(hand -> !hand.isCompleted()).findFirst().orElseThrow();
	}

	public void handleNewPlayer(ICasinoPlayer player) {
		if (table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS) {
			table.setStatus(com.casino.common.table.Status.RUNNING);
			startBetPhase();
		}
	}

	private boolean isAllowedToDealStartingHand() {
		return table.isGamePhase(GamePhase.BETS_COMPLETED) && somebodyHasBet();
	}

	private boolean somebodyHasBet() {
		return getPlayersWithBet() != null && getPlayersWithBet().size() > 0;
	}

	private List<ICasinoPlayer> getPlayersWithBet() {
		List<ICasinoPlayer> playersWithBet = table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().hasBet()).map(seat -> seat.getPlayer()).collect(Collectors.toList());
		return playersWithBet;
	}

	public void finalizeBetPhase() {
		table.getClock().stopClock();
		handlePlayers();
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
	}

	private void handlePlayers() {
		table.getSeats().stream().filter(seat -> seat.getPlayer() != null).map(seat -> seat.getPlayer()).forEach(player -> {
			player.setStatus(Status.SIT_OUT);
			if (!player.hasBet())
				table.changeFromPlayerToWatcher(player);
			else {
				player.subtractTotalBetFromBalance();
				player.setStatus(Status.ACTIVE);
				player.getHands().get(0).updateBet(player.getTotalBet());
			}
		});
	}

	public void updateStartingPlayer() {
		Optional<Seat> startingPlayer = table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getTotalBet() != null).min(Comparator.comparing(Seat::getNumber));
		if (startingPlayer.isEmpty()) {
			throw new IllegalStateException("Should start playing but no players with bet");
		}
		Seat nextSeat = startingPlayer.get();
		table.updatePlayerInTurn(nextSeat.getPlayer());
	}

	public void changeTurn() {
		Seat nextPlayer = table.getNextSeatWithBet();
		if (nextPlayer != null) {
			table.updatePlayerInTurn(nextPlayer.getPlayer());
			table.updateDealerTurn(false);
		} else {
			table.updateDealerTurn(true);
			table.updatePlayerInTurn(null);
		}
	}

	public void doubleDown(BlackjackPlayer player) {
		player.doubleDown();
	}

	public void handleSplit(BlackjackPlayer player) {
		player.splitStartingHand();
		player.getHands().get(0).addCard(decks.remove(decks.size() - 1));
	}

	public void stand(BlackjackPlayer player) {
		player.stand();
	}

	@Override
	public void playTurn() {
		// first test version
		while (!hand.isCompleted()) {
			try {
				System.out.println("dealer waits");
				Thread.sleep(Duration.of(500, ChronoUnit.MILLIS));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Card card = decks.remove(decks.size() - 1);
			System.out.println("dealer continues and takes card:" + card);
			addCard(card);
			System.out.println("Dealer hand value in first:" + hand.calculateValues().get(0));
		}
		System.out.println("Dealer hand is completed with value:" + hand.calculateValues().get(0));

	}
}
