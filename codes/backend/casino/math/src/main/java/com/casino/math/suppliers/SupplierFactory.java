package com.casino.math.suppliers;

import com.casino.common.ranges.Range;
import com.casino.common.runner.RunMode;

import java.util.function.Supplier;

public class SupplierFactory {

    public static Supplier<Integer> getIntegerSupplier(Range<Integer> range) {
        if (RunMode.isTestMode())
            return new FixedIntegerSupplier();
        return new RandomIntegerSupplier(range);
    }
}
