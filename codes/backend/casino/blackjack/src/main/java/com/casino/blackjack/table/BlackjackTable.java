package com.casino.blackjack.table;

import java.util.UUID;

import com.casino.blackjack.rules.Dealer;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetValues;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTable extends SeatedTable {
	private final Dealer dealer;

	public BlackjackTable(Status initialStatus, BetValues betValues, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betValues, playerLimit, type, seats, id);
		this.dealer = new Dealer(this, new BetInfo(betValues));
	}

	@SuppressWarnings("exports")
	public Dealer getDealer() {
		return dealer;
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
	public void onBetRoundEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public BetInfo getBetInfo() {
		return dealer.getBetInfo();
	}

}
