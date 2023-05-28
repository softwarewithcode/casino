package com.casino.common.reload;

import java.math.BigDecimal;
import java.util.UUID;

public interface Reloadable {

    BigDecimal tryFillUpToLimit(BigDecimal additionalAmount, BigDecimal fillUpLimit);

    UUID getId();

    String getUserName();
}
