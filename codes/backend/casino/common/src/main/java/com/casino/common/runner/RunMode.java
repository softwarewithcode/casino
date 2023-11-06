package com.casino.common.runner;

public interface RunMode {
	String TEST_RUNNER_DEFAULT_VALUE = "e7d2984d-8688-4b05-b392-73bc26acc16f";
	String TEST_RUNNER_WITH_FIXED_VALUE = TEST_RUNNER_DEFAULT_VALUE;

	static boolean isTestMode() {
		return CasinoMode.TEST_RUNNER;
	}
}
