package com.casino.roulette.persistence;

import java.util.UUID;

public record SpinResult(UUID spinId, Integer roundNumber, Integer winningNumber) {
}
