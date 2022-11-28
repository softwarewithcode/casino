package com.casino.common.player;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.casino.common.common.Result;

public class CasinoPlayer implements ICasinoPlayer {

	private String name;
	private UUID id;
	private BigDecimal startBalance;
	private BigDecimal endBalance;
	private BigDecimal balance;
	private BigDecimal bet;
	private Status status;

	public CasinoPlayer(String name, UUID id, BigDecimal startBalance) {
		super();
		this.name = name;
		this.id = id;
		this.startBalance = startBalance;
		this.status = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BigDecimal getInitialBalance() {
		return startBalance;
	}

	@Override
	public BigDecimal getEndBalance() {
		return endBalance;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CasinoPlayer other = (CasinoPlayer) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		// TODO Auto-generated method stub

	}

	@Override
	public BigDecimal getBet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateBet(BigDecimal bet) {
		boolean betAllowed = balance.compareTo(bet) >= 0;
		if (!betAllowed) {
			bet = null;
			return;
		}
		this.bet = bet;
	}

	@Override
	public void calculateBalance(Result result) {
		// TODO Auto-generated method stub

	}

	@Override
	public BigDecimal getBalance() {
		return balance;
	}

}
