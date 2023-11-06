package com.casino.roulette.bet;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.functions.Functions;
import com.casino.roulette.export.BetType;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "completed", "id", "data", "type", "scale" })
public class Bet implements RouletteBet {
	private final BetData data;
	private volatile BigDecimal winAmount;
	private volatile boolean completed;

	public Bet(BetData bet) {
		super();
		this.data = bet;
		this.winAmount = null;
	}

	@Override
	public synchronized void complete(Integer winningNumber) {
		if (completed)
			throw new IllegalArgumentException("Bet has been completed " + data.id() + " tried with new winningNumber:" + winningNumber);
		if (!EuropeanRouletteTable.getTableNumbersForBetPosition(data.position()).contains(winningNumber))
			winAmount = BigDecimal.ZERO;
		else
			winAmount = BigDecimal.valueOf(getScale()).multiply(data.amount());
		completed = true;
	}


	@Override
	public BigDecimal getAmount() {
		return data.amount();
	}

	@Override
	public BigDecimal getWinAmount() {
		if (!completed)
			return BigDecimal.ZERO;
		return winAmount; // Null if not completed
	}

	@Override
	public Integer getScale() {
		return data.betType().getPaysOut();
	}

	@Override
	public UUID getId() {
		return data.id();
	}

	@Override
	public BetType getType() {
		return data.betType();
	}

	@Override
	public Integer getPosition() {
		return data.position();
	}

	@Override
	public Bet replicate() {
		BetData data = new BetData(UUID.randomUUID(), getAmount(), getType(), getPosition());
		return new Bet(data);
	}

	@Override
	public String toString() {
		return "Bet [data=" + data + ", winAmount=" + winAmount + ", completed=" + completed + "]";
	}

	@Override
	public BetData getData() {
		return data;
	}

	@Override
	public Boolean success() {
		return completed && Functions.isFirstMoreThanSecond_(winAmount, BigDecimal.ZERO);
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

}
