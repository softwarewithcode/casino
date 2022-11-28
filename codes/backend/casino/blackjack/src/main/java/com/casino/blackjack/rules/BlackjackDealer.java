package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Stream;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetRoundTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.common.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.IDealer;
import com.casino.common.table.Phase;
import com.casino.common.table.Seat;

public class BlackjackDealer implements IDealer {
	private final BetInfo betInfo;
	private final BlackjackTable table;
	// 6 decks combined to one
	private List<Card> decks;

	public BlackjackDealer(BlackjackTable blackjackTable, BetInfo betInfo) {
		this.table = blackjackTable;
		this.betInfo = betInfo;
		this.decks = Deck.combineDecks(6);
	}

	public void initBetRound() {
		table.getPlayers().forEach(ICasinoPlayer::clearBet);
		table.updatePhase(Phase.BET_ROUND);
		initBetRoundTimer();
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

	private void initBetRoundTimer() {
		Timer timer = table.getTimer();
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		timer = new Timer();
		table.setTimer(timer);
		timer.schedule(new BetRoundTask(table), betInfo.getBetValues().initialBetRoundDelay(), 1000);
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

	public void welcomeNewPlayer(ICasinoPlayer player) {
		System.out.println("Dealer welcomes:" + player.getName());
		if (table.getReservedSeatCount() == 1) {
			initBetRound();
			System.out.println("Dealer starts betRound");
		}
	}
}
