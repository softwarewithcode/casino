package com.casino.common.tests;

import com.casino.common.ranges.Range;
import com.casino.common.runner.CasinoMode;
import com.casino.math.suppliers.SupplierFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class RangeTest {

    @Test
    public void integerRangeMinCannotBeMoreThanMax() {
        assertThrows(IllegalArgumentException.class, () -> new Range<>(2, 1));
    }

    @Test
    public void integerRangeMustContainTwoDifferentValues() {
        assertThrows(IllegalArgumentException.class, () -> new Range<>(2, 2));
    }

    @Test
    public void integerRangeMinMaxAreSet() {
        Range<Integer> range = new Range<>(2, 4);
        assertEquals(2, range.min());
        assertEquals(4, range.max());
    }

    @Test
    public void bigDecimalRangeMinCannotBeMoreThanMax() {
        BigDecimal min = new BigDecimal("1.1");
        BigDecimal max = new BigDecimal("0.99");
        assertThrows(IllegalArgumentException.class, () -> new Range<>(min, max));
    }

    @Test
    public void bigDecimalMustContainTwoDifferentValuesWithManyDecimals() {
        BigDecimal min = new BigDecimal("1.19373473437495");
        BigDecimal max = new BigDecimal("1.19373473437494");
        assertThrows(IllegalArgumentException.class, () -> new Range<>(min, max));
    }

    @Test
    public void bigDecimalMustContainTwoDifferentValues() {
        BigDecimal min = new BigDecimal("1.193");
        BigDecimal max = new BigDecimal("1.193");
        assertThrows(IllegalArgumentException.class, () -> new Range<>(min, max));
    }

    @Test
    public void bigdecimalRangeReturnsAllDecimals() {
        BigDecimal min = new BigDecimal("1.19373473437494");
        BigDecimal max = new BigDecimal("1.19373473437495");
        Range<BigDecimal> range = new Range<>(min, max);
        assertEquals(new BigDecimal("1.19373473437494"), range.min());
        assertEquals(new BigDecimal("1.19373473437495"), range.max());
    }

    @Test
    public void fixedIntegerSupplierReturnsExpectedInteger() {
        System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "12348");
        Range<Integer> range = new Range<>(1, 2);
        assertEquals(12348, SupplierFactory.getIntegerSupplier(range).get());
    }
}
