package com.casino.common.player;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.casino.common.common.Result;

public class CasinoPlayer implements ICasinoPlayer {

	private String name;
	private UUID id;
	private BigDecimal initialBalance;
	private BigDecimal endBalance;
	private BigDecimal balance;
	private BigDecimal bet;
	private Status status;

	public CasinoPlayer(String name, UUID id, BigDecimal initialBalance) {
		super();
		this.name = name;
		this.id = id;
		this.initialBalance = initialBalance;
		this.balance = initialBalance;
		this.status = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BigDecimal getInitialBalance() {
		return initialBalance;
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
		return bet;
	}

	@Override
	public void updateBet(BigDecimal bet) {
		// Bet data must be checked before calling this method. Here is no knowledge
		// about betLimits
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

	@Override
	public void clearBet() {
		this.bet = null;
	}

	@Override
	public void updateBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
