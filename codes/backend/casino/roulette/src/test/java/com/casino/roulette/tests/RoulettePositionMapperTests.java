package com.casino.roulette.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.roulette.export.BetPosition;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.casino.roulette.export.BetType;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoulettePositionMapperTests {

	@BeforeEach
	public void initTest() {
	}

	@Test
	public void position37ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(37);
		assertEquals(4, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 1, 2, 3)));
	}

	@Test
	public void position38ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(38);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 1)));
	}

	@Test
	public void position39ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(39);
		assertEquals(3, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 1, 2)));
	}

	@Test
	public void position40ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(40);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 2)));
	}

	@Test
	public void position41ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(41);
		assertEquals(3, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 2, 3)));
	}

	@Test
	public void position42ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(42);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(0, 3)));
	}

	@Test
	public void position43ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(43);
		assertEquals(3, numbers.size());
		assertTrue(numbers.containsAll(Set.of(1, 2, 3)));
	}

	@Test
	public void position44ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(44);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(1, 2)));
	}

	@Test
	public void position45ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(45);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(2, 3)));
	}

	@Test
	public void position46ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(46);
		assertEquals(6, numbers.size());
		assertTrue(numbers.containsAll(Set.of(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void position47ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(47);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(1, 4)));
	}

	@Test
	public void position48ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(48);
		assertEquals(4, numbers.size());
		assertTrue(numbers.containsAll(Set.of(1, 2, 4, 5)));
	}

	@Test
	public void position49ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(49);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(2, 5)));
	}

	@Test
	public void position50ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(50);
		assertEquals(4, numbers.size());
		assertTrue(numbers.containsAll(Set.of(2, 3, 5, 6)));
	}

	@Test
	public void position51ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(51);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(3, 6)));
	}

	@Test
	public void position52ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(52);
		assertEquals(3, numbers.size());
		assertTrue(numbers.containsAll(Set.of(4, 5, 6)));
	}

	@Test
	public void position53ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(53);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(4, 5)));
	}

	@Test
	public void position54ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(54);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(5, 6)));
	}

	@Test
	public void position55ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(55);
		assertEquals(6, numbers.size());
		assertTrue(numbers.containsAll(Set.of(4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void position56ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(56);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(4, 7)));
	}

	@Test
	public void position57ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(57);
		assertEquals(4, numbers.size());
		assertTrue(numbers.containsAll(Set.of(4, 5, 7, 8)));
	}

	@Test
	public void position58ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(58);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(5, 8)));
	}

	@Test
	public void position59ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(59);
		assertEquals(4, numbers.size());
		assertTrue(numbers.containsAll(Set.of(5, 6, 8, 9)));
	}

	@Test
	public void position60ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(60);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(6, 9)));
	}

	@Test
	public void position61ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(61);
		assertEquals(3, numbers.size());
		assertTrue(numbers.containsAll(Set.of(7, 8, 9)));
	}

	@Test
	public void position62ReturnValues() {
		List<Integer> numbers = EuropeanRouletteTable.getTableNumbersForBetPosition(62);
		assertEquals(2, numbers.size());
		assertTrue(numbers.containsAll(Set.of(7, 8)));
	}

	@Test
	public void betPosition140TableNumbersAndPayoutCheck() {
		BetPosition betPosition140 = EuropeanRouletteTable.getBetPositions().stream().filter(betPosition -> betPosition.number().equals(140)).findFirst().orElseThrow();
		assertEquals(betPosition140.number(), 140);
		assertEquals(betPosition140.tableNumbers().size(), 4);
		int[] expectedTableNumbers = { 32, 33, 35, 36 };
		assertArrayEquals(expectedTableNumbers, betPosition140.tableNumbers().stream().mapToInt(Integer::intValue).toArray());
		assertEquals(betPosition140.type(), BetType.QUADRUPLE_NUMBER);
	}
}
