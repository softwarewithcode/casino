package com.casino.roulette.table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.executors.ExecutorFactory;
import com.casino.math.suppliers.SupplierFactory;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.persistence.SpinResult;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = { "resultBoard", "spinId" })
public class Wheel implements RouletteWheel {
	private static final Logger LOGGER = Logger.getLogger(Wheel.class.getName());
	private final Supplier<Integer> randomNumberSupplier;
	private final Executor delayedExecutor;
	private final List<SpinResult> resultBoard;
	private final int historySize;
	private UUID spinId;

	public Wheel(RouletteData rouletteData) {
		resultBoard = new ArrayList<>(rouletteData.historySize() + 1);
		delayedExecutor = ExecutorFactory.getDelayedExecutor(rouletteData.spinTimeMillis(), TimeUnit.MILLISECONDS);
		randomNumberSupplier = SupplierFactory.getIntegerSupplier(rouletteData.tableNumbers());
		spinId = UUID.randomUUID();
		historySize = rouletteData.historySize();
	}

	@Override
	public List<SpinResult> getResultBoard() {
		return resultBoard.stream().toList();
	}

	@Override
	public UUID getSpinId() {
		return spinId;
	}

	@Override
	public CompletableFuture<SpinResult> spinBall() {
		CompletableFuture<SpinResult> spinResult = new CompletableFuture<>();
		CompletableFuture.supplyAsync(randomNumberSupplier, delayedExecutor).thenApply(this::finalizeSpin).thenRun(() -> spinResult.complete(resultBoard.getLast()));
		return spinResult;
	}

	private SpinResult finalizeSpin(Integer winningNumber) {
		resultBoard.add(new SpinResult(spinId, resultBoard.size() + 1, winningNumber));
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Wheel:finalizeSpin with:+" + resultBoard.getLast());
		if (resultBoard.size() > historySize)
			resultBoard.remove(0);
		return resultBoard.getLast();
	}

	@Override
	public void prepareNextSpin() {
		spinId = UUID.randomUUID();
	}

	@Override
	public SpinResult getResult() {
		return resultBoard.isEmpty() ? null : resultBoard.getLast();
	}
}
