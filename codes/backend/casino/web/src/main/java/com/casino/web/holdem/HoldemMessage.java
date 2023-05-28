package com.casino.web.holdem;

import java.math.BigDecimal;

import com.casino.poker.export.TexasHoldemCashGamePlayerAction;

import jakarta.json.bind.annotation.JsonbProperty;

public class HoldemMessage implements CasinoMessage{
    @JsonbProperty("action")
    private TexasHoldemCashGamePlayerAction action;
    @JsonbProperty("userId")
    private String userId;

    @JsonbProperty("seat")
    private String seat;

    @JsonbProperty("amount")
    private BigDecimal amount;

	public TexasHoldemCashGamePlayerAction getAction() {
        return action;
    }

    public void setAction(TexasHoldemCashGamePlayerAction action) {
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
        return "HoldemMessage [action=" + action + ", userId=" + userId + ", amount=" + amount + "]";
    }
}
