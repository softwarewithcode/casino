package com.casino.blackjack.rules;

import java.util.List;
import java.util.Timer;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetRoundTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.IDealer;

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

	public void startBetRound() {
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
			startBetRound();
			System.out.println("Dealer starts betRound");
		}
	}
}
