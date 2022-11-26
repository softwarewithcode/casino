package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.List;

import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.player.IPlayer;
import com.casino.common.table.OrderBasedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTable extends OrderBasedTable {
	// 6 decks combined to one
	private List<Card> decks;

	public BlackjackTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type, int seats) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type, seats);
		createDecks();
	}

	private void createDecks() {
		decks = Deck.combineDecks(6);
	}

	@Override
	public void onTimeout(IPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Card> getDecks() {
		return decks;
	}

	@Override
	public int getComputerTurnTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onPlayerLeave(IPlayer player) {
		// TODO Auto-generated method stub

	}

}
