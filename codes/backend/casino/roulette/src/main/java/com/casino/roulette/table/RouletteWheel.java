package com.casino.roulette.table;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.casino.roulette.persistence.SpinResult;

public interface RouletteWheel {

    void prepareNextSpin();

    List<SpinResult> getResultBoard();

    UUID getSpinId();

    CompletableFuture<SpinResult> spinBall();

    SpinResult getResult();
}
