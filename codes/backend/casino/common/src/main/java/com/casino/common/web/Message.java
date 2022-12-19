package com.casino.common.web;

import java.math.BigDecimal;

import com.casino.common.user.Action;

import jakarta.json.bind.annotation.JsonbProperty;

public class Message {
	@JsonbProperty("action")
	private Action action;
	@JsonbProperty("userId")
	private String userId;

	@JsonbProperty("seat")
	private String seat;

	@JsonbProperty("amount")
	private BigDecimal amount;

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getUserId() {
		return userId;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "BlackjackMessage [action=" + action + ", userId=" + userId + ", amount=" + amount + "]";
	}
}
