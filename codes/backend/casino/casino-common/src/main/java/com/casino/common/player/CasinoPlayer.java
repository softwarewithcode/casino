package com.casino.common.player;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class CasinoPlayer implements ICasinoPlayer {

	private String name;
	private UUID id;
	private BigDecimal startBalance;
	private BigDecimal endBalance;

	public CasinoPlayer(String name, UUID id, BigDecimal startBalance, BigDecimal endBalance) {
		super();
		this.name = name;
		this.id = id;
		this.startBalance = startBalance;
		this.endBalance = endBalance;
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

}
