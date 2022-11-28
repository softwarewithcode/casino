package com.casino.blackjack.rules;

import java.util.List;
import java.util.Timer;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetRoundTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;

public class Dealer {
	private final BetInfo betInfo;
	private final BlackjackTable table;
	// 6 decks combined to one
	private List<Card> decks;
	private Timer timer;

	public Dealer(BlackjackTable blackjackTable, BetInfo betInfo) {
		this.table = blackjackTable;
		this.betInfo = betInfo;
		this.decks = Deck.combineDecks(6);
	}

	public void startBetRound() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		timer = new Timer("betRound");
		timer.schedule(new BetRoundTask(table), 1000);
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
}
