package com.casino.common.player;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.common.cards.IHand;
import com.casino.common.table.ICasinoTable;

public interface ICasinoPlayer {
	public String getName();

	public BigDecimal getInitialBalance();

	public BigDecimal getEndBalance();

	public void increaseBalance(BigDecimal amount);

	public UUID getId();

	public void onLeave();

	public boolean canAct();

	public Status getStatus();

	public void setStatus(Status status);

	public BigDecimal getTotalBet();

	public boolean hasBet();

	public void updateStartingBet(BigDecimal bet, ICasinoTable table);

	public void reset();

	public void subtractTotalBetFromBalance();

	public BigDecimal getBalance();

	public List<IHand> getHands();

	public IHand getActiveHand();

	public void removeTotalBet();

	public boolean isWaitingForDealer();

}
