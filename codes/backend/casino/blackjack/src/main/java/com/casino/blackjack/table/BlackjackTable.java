package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.blackjack.rules.IBetRound;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.player.BetLimit;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTable extends SeatedTable implements IBetRound {
	// 6 decks combined to one
	private List<Card> decks;
	private BetRound betRound;

	public BlackjackTable(Status initialStatus, BetLimit betLimit, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betLimit, playerLimit, type, seats, id);
		createDecks();
	}

	public void bet(BigDecimal bet, ICasinoPlayer player) {

	}

	private void createDecks() {
		decks = Deck.combineDecks(6);
	}

	@Override
	public void onTimeout(ICasinoPlayer player) {
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
	public void onPlayerLeave(ICasinoPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBetRoundStart() {
		this.betRound = new BetRound(); // TODO timer for public tables

	}

}
