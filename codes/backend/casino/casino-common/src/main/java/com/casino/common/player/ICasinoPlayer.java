package com.casino.common.player;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.common.cards.IHand;
import com.casino.common.common.Result;
import com.casino.common.table.ICasinoTable;

public interface ICasinoPlayer {
	public String getName();

	public BigDecimal getInitialBalance();

	public BigDecimal getEndBalance();

	public UUID getId();

	public void onLeave();

	public Status getStatus();

	public void setStatus(Status status);

	public BigDecimal getBet();

	public void updateBet(BigDecimal bet, ICasinoTable table);

	public void clearBet();

	public void calculateBalance(Result result);

	public BigDecimal getBalance();

	public void updateBalance(BigDecimal balance);

	public List<IHand> getHands();
}
