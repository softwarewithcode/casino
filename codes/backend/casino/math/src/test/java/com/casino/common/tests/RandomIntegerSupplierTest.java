package com.casino.common.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.casino.common.ranges.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.math.suppliers.RandomIntegerSupplier;

public class RandomIntegerSupplierTest {
	private List<Integer> allowedValues;
	private final Predicate<Integer> isAllowedValue = i -> allowedValues.contains(i);
	private Range<Integer> range;
	private List<Integer> generatedValues;
	private Supplier<Integer> supplier;
	private final Integer ITERATIONS = 10000;

	@BeforeEach
	public void clean() {
		allowedValues = null;
		range = null;
		generatedValues = new ArrayList<>();
	}

	private void initGeneratorRange(Integer minVal, Integer maxValue) {
		range = new Range<>(minVal, maxValue);
		supplier = new RandomIntegerSupplier(range);
		allowedValues = IntStream.rangeClosed(minVal, maxValue).boxed().toList();
	}

	@Test
	public void allGeneratedRandomIntegersAreInRange() {
		initGeneratorRange(543, 544);
		IntStream.rangeClosed(0, ITERATIONS).forEach(i -> generatedValues.add(supplier.get()));
		assertAllGeneratedValuesAreAllowed();
	}

	private void assertAllGeneratedValuesAreAllowed() {
		assertTrue(generatedValues.stream().allMatch(isAllowedValue));
	}

	@Test
	public void randomNumberGeneratorReachesMinimumFrequencyExpectations() {
		initGeneratorRange(10, 12);
		IntStream.rangeClosed(0, 10).forEach(i -> {
			generatedValues = new ArrayList<>();
			System.out.println("** RandomNumberTest minimumFrequencies iteration=" + i + " runs=" + ITERATIONS);
			IntStream.rangeClosed(1, ITERATIONS).forEach(j -> generatedValues.add(supplier.get()));
			printMinMaxTestResults();
			assertEquals(10000, generatedValues.size());
			assertTrue(Collections.frequency(generatedValues, 10) > 2500);
			assertTrue(Collections.frequency(generatedValues, 11) > 2500);
			assertTrue(Collections.frequency(generatedValues, 12) > 2500);
			assertAllGeneratedValuesAreAllowed();
		});
	}

	@Test
	public void randomNumberGeneratorReachesMaximumFrequencyExpectations() {
		initGeneratorRange(10, 12);
		IntStream.rangeClosed(0, 10).forEach(i -> {
			generatedValues = new ArrayList<>();
			System.out.println("** RandomNumberTest maximumFrequencies iteration=" + i + " runs=" + ITERATIONS);
			IntStream.range(0, ITERATIONS).forEach(j -> generatedValues.add(supplier.get()));
			printMinMaxTestResults();
			assertEquals(10000, generatedValues.size());
			assertTrue(Collections.frequency(generatedValues, 10) < 5000);
			assertTrue(Collections.frequency(generatedValues, 11) < 5000);
			assertTrue(Collections.frequency(generatedValues, 12) < 5000);
			assertAllGeneratedValuesAreAllowed();
		});
	}

	@Test
	public void eachEuropeanRouletteTableNumberIsGenerated() {
		int highestNumberOnBoard = 36;
		initGeneratorRange(0, highestNumberOnBoard);
		generatedValues = new ArrayList<>();
		IntStream.range(0, 10).forEach(i -> {
			generatedValues = new ArrayList<>();
			System.out.println("** RandomNumberTest europeanRouletteTableNumbers iteration=" + i + " runs=" + ITERATIONS);
			IntStream.range(0, ITERATIONS).forEach(j -> generatedValues.add(supplier.get()));
			assertEquals(10000, generatedValues.size());
			IntStream.rangeClosed(0, highestNumberOnBoard).forEach(number -> {
				System.out.println("RouletteBoard GeneratedFrequencies: " + number + " -> " + Collections.frequency(generatedValues, number));
				assertTrue(generatedValues.contains(number));
			});
			assertAllGeneratedValuesAreAllowed();
		});
	}

	private void printMinMaxTestResults() {
		System.out.println(" GeneratedFrequencies: 10=" + Collections.frequency(generatedValues, 10));
		System.out.println(" GeneratedFrequencies: 11=" + Collections.frequency(generatedValues, 11));
		System.out.println(" GeneratedFrequencies: 12=" + Collections.frequency(generatedValues, 12));
	}
}
