package com.casino.roulette.persistence;

import java.util.UUID;

public record RoundResult(SpinResult spinResult, PlayerResult playerResult, UUID tableId) {

}
