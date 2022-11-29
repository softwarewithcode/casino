package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.common.bet.BetInfo;
import com.casino.common.bet.BetUtil;
import com.casino.common.bet.BetValues;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.Phase;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.SeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTable extends SeatedTable {
	private final BlackjackDealer dealer;
	private volatile Phase phase;

	public BlackjackTable(Status initialStatus, BetValues betValues, PlayerRange playerLimit, Type type, int seats, UUID id) {
		super(initialStatus, betValues, playerLimit, type, seats, id);
		this.dealer = new BlackjackDealer(this, new BetInfo(betValues));
	}

	@SuppressWarnings("exports")
	@Override
	public BlackjackDealer getDealer() {
		return dealer;
	}

	public void updatePhase(Phase phase) {
		if (this.phase != phase)
			this.phase = phase;
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

	@SuppressWarnings("preview")
	@Override
	public void onBetRoundEnd() {
		dealer.finalizeBetPhase();
		System.out.println("CurrentThread:onBetRoundEnd" + Thread.currentThread().toString());
		if (dealer.shouldMakeInitialDeal()) {
			Thread.ofVirtual().start(() -> dealer.dealInitialCards()); // testing here and commenting below row
			// dealer.dealInitialCards();
		}
		System.out.println("onBetRoundEnd End");
	}

	@Override
	public BetInfo getBetInfo() {
		return dealer.getBetInfo();
	}

	public Phase getPhase() {
		return phase;
	}

	@Override
	public String toString() {
		return "BlackjackTable [dealer=" + dealer + ", phase=" + phase + "]";
	}

}
