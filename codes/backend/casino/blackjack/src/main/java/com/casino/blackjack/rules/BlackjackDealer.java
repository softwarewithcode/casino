package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetPhaseClockTask;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Seat;
import com.casino.common.table.phase.GamePhase;

public class BlackjackDealer implements IDealer {
	private final BetThresholds betThresholds;
	private final BlackjackTable table;
	private List<Card> decks; // 6 decks

	public BlackjackDealer(BlackjackTable blackjackTable, BetThresholds betThresholds) {
		this.table = blackjackTable;
		this.betThresholds = betThresholds;
		this.decks = Deck.combineDecks(6);
	}

	private void startBetPhase() {
		table.updateGamePhase(GamePhase.BET);
		System.out.println("StartBetPhase:" + table.getGamePhase() + " -> ");
		if (table.getActivePlayerCount() > 0) {
			BetPhaseClockTask task = new BetPhaseClockTask(table);
			this.getTable().getClock().startClock(task, 1000);
		}
	}

	public void handlePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.getPlayer() != null && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateBet(bet, table);
		}, () -> {
			throw new PlayerNotFoundException("Player not found in table:" + table + " player:" + tablePlayer, 1);
		});
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
		if (!isAllowedToDeal())
			return false;
		IntStream.range(0, 2).forEach(i -> getPlayersWithBet().forEach(player -> dealCard(player, decks.remove(decks.size() - 1))));
		return true;
	}

	private void dealCard(ICasinoPlayer player, Card card) {
		System.out.println("Dealer dealCard " + player + " getsCard:" + card);
		player.getHands().get(0).addCard(card);
	}

	public void addCard(ICasinoPlayer player) {
		if (!table.isGamePhase(GamePhase.PLAY))
			throw new IllegalPhaseException("Wrong phase for add card", table.getGamePhase(), GamePhase.PLAY);
		Card card = decks.remove(decks.size() - 1);
		getActiveHand(player).addCard(card);
	}

	private IHand getActiveHand(ICasinoPlayer player) {
		return player.getHands().stream().filter(hand -> hand.isActive()).findFirst().orElseThrow();
	}

	public void handleNewPlayer(ICasinoPlayer player) {
		System.out.println("Dealer welcomes:" + player + " currentPlayers:" + table.getPlayers().size() + "PHASE:" + table.getGamePhase() + " table:" + table.getId());
		if (table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS) {
			table.setStatus(com.casino.common.table.Status.RUNNING);
			startBetPhase();
		}
	}

	private boolean isAllowedToDeal() {
		if (!table.isGamePhase(GamePhase.BETS_COMPLETED))
			throw new IllegalPhaseException("Wrong phase for card dealing", table.getGamePhase(), GamePhase.BETS_COMPLETED);
		return somebodyHasBet();
	}

	private boolean somebodyHasBet() {
		return getPlayersWithBet() != null && getPlayersWithBet().size() > 0;
	}

	private List<ICasinoPlayer> getPlayersWithBet() {
		List<ICasinoPlayer> playersWithBet = table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getBet() != null).map(seat -> seat.getPlayer()).collect(Collectors.toList());
		return playersWithBet;
	}

	public void finalizeBetPhase() {
		table.getClock().stopClock();
		updatePlayers();
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
	}

	private void updatePlayers() {
		table.getSeats().stream().filter(seat -> seat.getPlayer() != null).map(seat -> seat.getPlayer()).forEach(player -> {
			if (player.getBet() == null) {
				player.setStatus(Status.SIT_OUT);
				table.changeFromPlayerToWatcher(player);
			} else
				player.setStatus(Status.AVAILABLE);
		});
	}

	public void updateStartingPlayer() {
		Optional<Seat> startingPlayer = table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getBet() != null).min(Comparator.comparing(Seat::getNumber));
		if (startingPlayer.isEmpty()) {
			throw new IllegalStateException("Should start playing but no players with bet");
		}
		Seat nextSeat = startingPlayer.get();
		table.setPlayerInTurn(nextSeat.getPlayer());
	}

	public void changeTurn() {
		// TODO Auto-generated method stub

	}
}
