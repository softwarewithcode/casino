package com.casino.web.common;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

//Represents message from enduser to server including exception in RouletteEndpoint 05.10.2023
@JsonInclude(Include.NON_NULL)
public abstract class ClientMessage implements Message {

	private String seat;
	private BigDecimal amount;

	public String getSeat() {
		return seat;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}