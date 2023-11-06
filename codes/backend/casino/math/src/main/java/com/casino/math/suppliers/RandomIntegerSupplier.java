package com.casino.math.suppliers;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.casino.common.ranges.Range;

/**
 * @author softwarewithcode from GitHub
 */
public final class RandomIntegerSupplier implements Supplier<Integer> {
	private final Range<Integer> range;

	public RandomIntegerSupplier(Range<Integer> rangeInclusive) {
		this.range = rangeInclusive;
	}

	@Override
	public Integer get() {
		return ThreadLocalRandom.current().nextInt(range.min(), range.max() + 1);
	}
}
