package com.casino.common.runner;

public final class CasinoMode implements RunMode {

	public static final Boolean TEST_RUNNER;
	static {
		String testMode = (String) System.getProperties().get(RunMode.TEST_RUNNER_WITH_FIXED_VALUE);
		TEST_RUNNER = testMode != null && !testMode.equals(RunMode.TEST_RUNNER_DEFAULT_VALUE);
	}

}