package com.casino.common.executors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ExecutorFactory {
	public static Executor getDelayedExecutor(Long delay, TimeUnit timeUnit) {
		return CompletableFuture.delayedExecutor(delay, timeUnit);
	}
}