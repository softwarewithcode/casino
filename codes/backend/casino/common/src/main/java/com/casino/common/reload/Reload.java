package com.casino.common.reload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Reload {
    private final ReloadData input;
    private BigDecimal usedAmount;

    public Reload(ReloadData input) {
        this.input = input;
    }

    public ReloadData getInput() {
        return input;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount.setScale(2,RoundingMode.DOWN);
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }
    
    public UUID getId() {
    	return input.reloadId();
    }
}
