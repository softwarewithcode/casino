package com.casino.common.reload;

import com.casino.common.functions.Functions;

import java.math.BigDecimal;
import java.util.UUID;

public record ReloadData(UUID reloadId, Reloadable reloadable, BigDecimal reloadAmountAttempt, BigDecimal upToLimit) {

    public ReloadData {
        if (Functions.isFirstMoreOrEqualToSecond_(BigDecimal.ZERO, reloadAmountAttempt))
            throw new IllegalArgumentException("Reload attempt must be greater than zero");
    }
}
