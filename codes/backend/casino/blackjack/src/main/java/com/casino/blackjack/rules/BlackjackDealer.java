package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetPhaseClockTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Seat;
import com.casino.common.table.phase.GamePhase;

public class BlackjackDealer implements IDealer {
	private final BetInfo betInfo;
	private final BlackjackTable table;
	private List<Card> decks; // 6 decks

	public BlackjackDealer(BlackjackTable blackjackTable, BetInfo betInfo) {
		this.table = blackjackTable;
		this.betInfo = betInfo;
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

	public void setDecks(List<Card> decks) {
		this.decks = decks;
	}

	public BetInfo getBetInfo() {
		return betInfo;
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
		updateSittingOutAndAvailablePlayers();
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
	}

	private void updateSittingOutAndAvailablePlayers() {
		table.getSeats().stream().filter(seat -> seat.getPlayer() != null).map(seat -> seat.getPlayer()).forEach(player -> {
			if (player.getBet() == null) {
				player.setStatus(Status.SIT_OUT);
				table.changeFromPlayerToWatcher(player);
			} else
				player.setStatus(Status.AVAILABLE);
		});
	}

	public void updatePlayerInTurn() {
		Optional<Seat> startingSeat = table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getBet() != null).min(Comparator.comparing(Seat::getNumber));
		if (startingSeat.isEmpty()) {
			throw new IllegalStateException("Should start playing but no players with bet");
		}
		Seat starting = startingSeat.get();
		System.out.println("Starting player is:" + starting.getPlayer());
	}

	public void returnBets() {
		// TODO Auto-generated method stub

	}
}
