package com.casino.math.suppliers;

import com.casino.common.runner.RunMode;

import java.util.function.Supplier;


public class FixedIntegerSupplier implements Supplier<Integer> {
    // Returns only predefined values
    @Override
    public Integer get() {
        String fixedVal = (String) System.getProperties().get(RunMode.TEST_RUNNER_WITH_FIXED_VALUE);
        return Integer.valueOf(fixedVal);
    }
}
