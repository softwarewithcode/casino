package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetPhaseClockTask;
import com.casino.common.bet.BetUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.common.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Phase;
import com.casino.common.table.Seat;

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
		table.updatePhase(Phase.BET);
		if (table.getActivePlayerCount() > 0) {
			BetPhaseClockTask task = new BetPhaseClockTask(table);
			this.getTable().getClock().startClock(task, 1000);
		}
	}

	public void handlePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		BetUtil.verifyBet(table, tablePlayer, bet);
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.getPlayer() != null && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateBet(bet);
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

	public void dealInitialCards() {
		System.out.println("dealInitialCards ");
		table.updatePhase(Phase.INITIAL_DEAL);
		// getPlayersWithBet().forEach(player -> dealCard(player,
		// decks.remove(decks.size() - 1)));
		IntStream.range(0, 2).forEach(i -> getPlayersWithBet().forEach(player -> dealCard(player, decks.remove(decks.size() - 1))));
		table.updatePhase(Phase.INITIAL_DEAL_COMPLETED);
	}

	private void dealCard(ICasinoPlayer player, Card card) {
		System.out.println("Player " + player + " getsCard:" + card);
		player.getHands().get(0).addCard(card);
	}

	public synchronized void handleNewPlayer(ICasinoPlayer player) {
		System.out.println("Dealer welcomes:" + player);
		if (table.getPhase() == null) {
			startBetPhase();
		}
	}

	public boolean shouldMakeInitialDeal() {
		return table.getPhase() == Phase.BET_COMPLETED && somebodyHasBet();
	}

	private boolean somebodyHasBet() {
		return getPlayersWithBet() != null;
	}

	private List<ICasinoPlayer> getPlayersWithBet() {
		return table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getBet() != null).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public void finalizeBetPhase() {
		table.updatePhase(Phase.BET_COMPLETED);
		table.getClock().stopClock();
		updatePlayerStatusesAfterBetPhase();
	}

	private void updatePlayerStatusesAfterBetPhase() {
		table.getSeats().stream().filter(seat -> seat.getPlayer() != null).map(seat -> seat.getPlayer()).forEach(player -> {
			if (player.getBet() == null)
				player.setStatus(Status.SIT_OUT);
			else
				player.setStatus(Status.AVAILABLE);
		});
	}
}
