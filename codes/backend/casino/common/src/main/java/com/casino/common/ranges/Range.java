package com.casino.common.ranges;

public record Range<T extends Number>(T min, T max) implements Rangeable<T> {
	public Range {
		if (min.doubleValue() >= max.doubleValue())
			throw new IllegalArgumentException("invalid inputs for number range from:" + min + " to " + max);
	}

}