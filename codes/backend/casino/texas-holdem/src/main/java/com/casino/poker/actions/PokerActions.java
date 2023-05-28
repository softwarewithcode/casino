package com.casino.poker.actions;

import java.math.BigDecimal;
import java.util.List;

public record PokerActions(List<PokerActionType> actions, BigDecimal min) {
}
