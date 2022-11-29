package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetPhaseClockTask;
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
	private static final Integer SECOND_IN_MILLIS = 1000;
	// 6 decks combined to one
	private List<Card> decks;

	public BlackjackDealer(BlackjackTable blackjackTable, BetInfo betInfo) {
		this.table = blackjackTable;
		this.betInfo = betInfo;
		this.decks = Deck.combineDecks(6);
	}

	private void startBetPhase() {
		table.updatePhase(Phase.BET);
		if (table.getActivePlayerCount() > 0) {
			BetPhaseClockTask task = new BetPhaseClockTask(table);
			this.getTable().getClock().startClock(task, 200);
		}
	}

	public void placeBetForPlayer(ICasinoPlayer tablePlayer, BigDecimal bet) {
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

	public void makeInitialDeal() {
		table.updatePhase(Phase.INITIAL_DEAL);
	}

	public synchronized void welcomeNewPlayer(ICasinoPlayer player) {
		System.out.println("Dealer welcomes:" + player.getName());
		if (table.getReservedSeatCount() == 1) {
			startBetPhase();
			System.out.println("Dealer starts betRound");
		} else if (table.getPhase() == Phase.BET) {
			startBetPhase();
		}
	}

	public boolean shouldMakeInitialDeal() {
		return table.getPhase() == Phase.INITIAL_DEAL && somebodyHasBet();
	}

	private boolean somebodyHasBet() {
		return getPlayersWithBet().findAny().isPresent();
	}

	private Stream<Seat> getPlayersWithBet() {
		return table.getSeats().stream().filter(seat -> seat.getPlayer() != null && seat.getPlayer().getBet() != null);
	}

	public void finalizeBetPhase() {
		table.updatePhase(null);
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
