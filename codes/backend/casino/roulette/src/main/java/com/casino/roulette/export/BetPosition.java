package com.casino.roulette.export;

import java.util.List;

public record BetPosition(Integer number, List<Integer> tableNumbers, BetType type) {

}
