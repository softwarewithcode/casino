package com.casino.poker.table;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = { "seatNumber" })
public class DealerButton {

    private Integer seatNumber;

    public DealerButton(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void moveButton(Integer seatNumber) {
        if (seatNumber == null)
            throw new IllegalArgumentException("Keep dealerButton on table");
        this.seatNumber = seatNumber;
    }
}
