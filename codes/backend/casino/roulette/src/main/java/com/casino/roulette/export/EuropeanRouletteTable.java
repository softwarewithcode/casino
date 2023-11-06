package com.casino.roulette.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.casino.common.validation.Verifier;

public class EuropeanRouletteTable {
	// Visualization of positions from the documentation.
	// regex -> case \d+ -> integers =
	public static final Map<Integer, List<Integer>> BET_POSITION_NUMBERS;
	public static final List<BetPosition> BET_POSITIONS;
	static {
		BET_POSITION_NUMBERS = new HashMap<>();
		BET_POSITION_NUMBERS.put(0, List.of(0));
		BET_POSITION_NUMBERS.put(1, List.of(1));
		BET_POSITION_NUMBERS.put(2, List.of(2));
		BET_POSITION_NUMBERS.put(3, List.of(3));
		BET_POSITION_NUMBERS.put(4, List.of(4));
		BET_POSITION_NUMBERS.put(5, List.of(5));
		BET_POSITION_NUMBERS.put(6, List.of(6));
		BET_POSITION_NUMBERS.put(7, List.of(7));
		BET_POSITION_NUMBERS.put(8, List.of(8));
		BET_POSITION_NUMBERS.put(9, List.of(9));
		BET_POSITION_NUMBERS.put(10, List.of(10));
		BET_POSITION_NUMBERS.put(11, List.of(11));
		BET_POSITION_NUMBERS.put(12, List.of(12));
		BET_POSITION_NUMBERS.put(13, List.of(13));
		BET_POSITION_NUMBERS.put(14, List.of(14));
		BET_POSITION_NUMBERS.put(15, List.of(15));
		BET_POSITION_NUMBERS.put(16, List.of(16));
		BET_POSITION_NUMBERS.put(17, List.of(17));
		BET_POSITION_NUMBERS.put(18, List.of(18));
		BET_POSITION_NUMBERS.put(19, List.of(19));
		BET_POSITION_NUMBERS.put(20, List.of(20));
		BET_POSITION_NUMBERS.put(21, List.of(21));
		BET_POSITION_NUMBERS.put(22, List.of(22));
		BET_POSITION_NUMBERS.put(23, List.of(23));
		BET_POSITION_NUMBERS.put(24, List.of(24));
		BET_POSITION_NUMBERS.put(25, List.of(25));
		BET_POSITION_NUMBERS.put(26, List.of(26));
		BET_POSITION_NUMBERS.put(27, List.of(27));
		BET_POSITION_NUMBERS.put(28, List.of(28));
		BET_POSITION_NUMBERS.put(29, List.of(29));
		BET_POSITION_NUMBERS.put(30, List.of(30));
		BET_POSITION_NUMBERS.put(31, List.of(31));
		BET_POSITION_NUMBERS.put(32, List.of(32));
		BET_POSITION_NUMBERS.put(33, List.of(33));
		BET_POSITION_NUMBERS.put(34, List.of(34));
		BET_POSITION_NUMBERS.put(35, List.of(35));
		BET_POSITION_NUMBERS.put(36, List.of(36));
		BET_POSITION_NUMBERS.put(37, List.of(0, 1, 2, 3));
		BET_POSITION_NUMBERS.put(38, List.of(0, 1));
		BET_POSITION_NUMBERS.put(39, List.of(0, 1, 2));
		BET_POSITION_NUMBERS.put(40, List.of(0, 2));
		BET_POSITION_NUMBERS.put(41, List.of(0, 2, 3));
		BET_POSITION_NUMBERS.put(42, List.of(0, 3));
		BET_POSITION_NUMBERS.put(43, List.of(1, 2, 3));
		BET_POSITION_NUMBERS.put(44, List.of(1, 2));
		BET_POSITION_NUMBERS.put(45, List.of(2, 3));
		BET_POSITION_NUMBERS.put(46, List.of(1, 2, 3, 4, 5, 6));
		BET_POSITION_NUMBERS.put(47, List.of(1, 4));
		BET_POSITION_NUMBERS.put(48, List.of(1, 2, 4, 5));
		BET_POSITION_NUMBERS.put(49, List.of(2, 5));
		BET_POSITION_NUMBERS.put(50, List.of(2, 3, 5, 6));
		BET_POSITION_NUMBERS.put(51, List.of(3, 6));
		BET_POSITION_NUMBERS.put(52, List.of(4, 5, 6));
		BET_POSITION_NUMBERS.put(53, List.of(4, 5));
		BET_POSITION_NUMBERS.put(54, List.of(5, 6));
		BET_POSITION_NUMBERS.put(55, List.of(4, 5, 6, 7, 8, 9));
		BET_POSITION_NUMBERS.put(56, List.of(4, 7));
		BET_POSITION_NUMBERS.put(57, List.of(4, 5, 7, 8));
		BET_POSITION_NUMBERS.put(58, List.of(5, 8));
		BET_POSITION_NUMBERS.put(59, List.of(5, 6, 8, 9));
		BET_POSITION_NUMBERS.put(60, List.of(6, 9));
		BET_POSITION_NUMBERS.put(61, List.of(7, 8, 9));
		BET_POSITION_NUMBERS.put(62, List.of(7, 8));
		BET_POSITION_NUMBERS.put(63, List.of(8, 9));
		BET_POSITION_NUMBERS.put(64, List.of(7, 8, 9, 10, 11, 12));
		BET_POSITION_NUMBERS.put(65, List.of(7, 10));
		BET_POSITION_NUMBERS.put(66, List.of(7, 8, 10, 11));
		BET_POSITION_NUMBERS.put(67, List.of(8, 11));
		BET_POSITION_NUMBERS.put(68, List.of(8, 9, 11, 12));
		BET_POSITION_NUMBERS.put(69, List.of(9, 12));
		BET_POSITION_NUMBERS.put(70, List.of(10, 11, 12));
		BET_POSITION_NUMBERS.put(71, List.of(10, 11));
		BET_POSITION_NUMBERS.put(72, List.of(11, 12));
		BET_POSITION_NUMBERS.put(73, List.of(10, 11, 12, 13, 14, 15));
		BET_POSITION_NUMBERS.put(74, List.of(10, 13));
		BET_POSITION_NUMBERS.put(75, List.of(10, 11, 13, 14));
		BET_POSITION_NUMBERS.put(76, List.of(11, 14));
		BET_POSITION_NUMBERS.put(77, List.of(11, 12, 14, 15));
		BET_POSITION_NUMBERS.put(78, List.of(12, 15));
		BET_POSITION_NUMBERS.put(79, List.of(13, 14, 15));
		BET_POSITION_NUMBERS.put(80, List.of(13, 14));
		BET_POSITION_NUMBERS.put(81, List.of(14, 15));
		BET_POSITION_NUMBERS.put(82, List.of(13, 14, 15, 16, 17, 18));
		BET_POSITION_NUMBERS.put(83, List.of(13, 16));
		BET_POSITION_NUMBERS.put(84, List.of(13, 14, 16, 17));
		BET_POSITION_NUMBERS.put(85, List.of(14, 17));
		BET_POSITION_NUMBERS.put(86, List.of(14, 15, 17, 18));
		BET_POSITION_NUMBERS.put(87, List.of(15, 18));
		BET_POSITION_NUMBERS.put(88, List.of(16, 17, 18));
		BET_POSITION_NUMBERS.put(89, List.of(16, 17));
		BET_POSITION_NUMBERS.put(90, List.of(17, 18));
		BET_POSITION_NUMBERS.put(91, List.of(16, 17, 18, 19, 20, 21));
		BET_POSITION_NUMBERS.put(92, List.of(16, 19));
		BET_POSITION_NUMBERS.put(93, List.of(16, 17, 19, 20));
		BET_POSITION_NUMBERS.put(94, List.of(17, 20));
		BET_POSITION_NUMBERS.put(95, List.of(17, 18, 20, 21));
		BET_POSITION_NUMBERS.put(96, List.of(18, 21));
		BET_POSITION_NUMBERS.put(97, List.of(19, 20, 21));
		BET_POSITION_NUMBERS.put(98, List.of(19, 20));
		BET_POSITION_NUMBERS.put(99, List.of(20, 21));
		BET_POSITION_NUMBERS.put(100, List.of(19, 20, 21, 22, 23, 24));
		BET_POSITION_NUMBERS.put(101, List.of(19, 22));
		BET_POSITION_NUMBERS.put(102, List.of(19, 20, 22, 23));
		BET_POSITION_NUMBERS.put(103, List.of(20, 23));
		BET_POSITION_NUMBERS.put(104, List.of(20, 21, 23, 24));
		BET_POSITION_NUMBERS.put(105, List.of(21, 24));
		BET_POSITION_NUMBERS.put(106, List.of(22, 23, 24));
		BET_POSITION_NUMBERS.put(107, List.of(22, 23));
		BET_POSITION_NUMBERS.put(108, List.of(23, 24));
		BET_POSITION_NUMBERS.put(109, List.of(22, 23, 24, 25, 26, 27));
		BET_POSITION_NUMBERS.put(110, List.of(22, 25));
		BET_POSITION_NUMBERS.put(111, List.of(22, 23, 25, 26));
		BET_POSITION_NUMBERS.put(112, List.of(23, 26));
		BET_POSITION_NUMBERS.put(113, List.of(23, 24, 26, 27));
		BET_POSITION_NUMBERS.put(114, List.of(24, 27));
		BET_POSITION_NUMBERS.put(115, List.of(25, 26, 27));
		BET_POSITION_NUMBERS.put(116, List.of(25, 26));
		BET_POSITION_NUMBERS.put(117, List.of(26, 27));
		BET_POSITION_NUMBERS.put(118, List.of(25, 26, 27, 28, 29, 30));
		BET_POSITION_NUMBERS.put(119, List.of(25, 28));
		BET_POSITION_NUMBERS.put(120, List.of(25, 26, 28, 29));
		BET_POSITION_NUMBERS.put(121, List.of(26, 29));
		BET_POSITION_NUMBERS.put(122, List.of(26, 27, 29, 30));
		BET_POSITION_NUMBERS.put(123, List.of(27, 30));
		BET_POSITION_NUMBERS.put(124, List.of(28, 29, 30));
		BET_POSITION_NUMBERS.put(125, List.of(28, 29));
		BET_POSITION_NUMBERS.put(126, List.of(29, 30));
		BET_POSITION_NUMBERS.put(127, List.of(26, 29, 30, 31, 32, 33));
		BET_POSITION_NUMBERS.put(128, List.of(28, 31));
		BET_POSITION_NUMBERS.put(129, List.of(28, 29, 31, 32));
		BET_POSITION_NUMBERS.put(130, List.of(29, 32));
		BET_POSITION_NUMBERS.put(131, List.of(29, 30, 32, 33));
		BET_POSITION_NUMBERS.put(132, List.of(30, 33));
		BET_POSITION_NUMBERS.put(133, List.of(31, 32, 33));
		BET_POSITION_NUMBERS.put(134, List.of(31, 32));
		BET_POSITION_NUMBERS.put(135, List.of(32, 33));
		BET_POSITION_NUMBERS.put(136, List.of(31, 32, 33, 34, 35, 36));
		BET_POSITION_NUMBERS.put(137, List.of(31, 34));
		BET_POSITION_NUMBERS.put(138, List.of(31, 32, 34, 35));
		BET_POSITION_NUMBERS.put(139, List.of(32, 35));
		BET_POSITION_NUMBERS.put(140, List.of(32, 33, 35, 36));
		BET_POSITION_NUMBERS.put(141, List.of(33, 36));
		BET_POSITION_NUMBERS.put(142, List.of(34, 35, 36));
		BET_POSITION_NUMBERS.put(143, List.of(34, 35));
		BET_POSITION_NUMBERS.put(144, List.of(35, 36));
		BET_POSITION_NUMBERS.put(145, List.of(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34)); // first column
		BET_POSITION_NUMBERS.put(146, List.of(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35)); // second column
		BET_POSITION_NUMBERS.put(147, List.of(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36)); // third column
		BET_POSITION_NUMBERS.put(200, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)); // first dozen
		BET_POSITION_NUMBERS.put(201, List.of(13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24)); // second dozen
		BET_POSITION_NUMBERS.put(202, List.of(25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36)); // third dozen
		BET_POSITION_NUMBERS.put(203, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18));// first half
		BET_POSITION_NUMBERS.put(204, List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36));// even
		BET_POSITION_NUMBERS.put(205, List.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36));// red
		BET_POSITION_NUMBERS.put(206, List.of(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35));// black
		BET_POSITION_NUMBERS.put(207, List.of(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35));// odd
		BET_POSITION_NUMBERS.put(208, List.of(19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36));// second half

		BET_POSITIONS = new ArrayList<>();
		BET_POSITION_NUMBERS.forEach((key, value) -> BET_POSITIONS.add(new BetPosition(key, BET_POSITION_NUMBERS.get(key), verifyPositionAndGetBetPositionType(key))));
	}

	public static List<BetPosition> getBetPositions() {
		return BET_POSITIONS;
	}

	public static List<Integer> getTableNumbersForBetPosition(Integer betPosition) {
		validateGivenParameter(betPosition);
		List<Integer> positions = BET_POSITION_NUMBERS.get(betPosition);
		if (positions == null)
			throw new IllegalArgumentException("No such position " + betPosition);
		return positions;
	}

	public static BetType verifyPositionAndGetBetPositionType(Integer betPosition) {
		Verifier.verifyNotNull(betPosition);
		if (betPosition >= 0 && betPosition <= 36)
			return BetType.SINGLE_NUMBER;
		BetType type;
		switch (betPosition) {
		case 37, 48, 50, 57, 59, 66, 68, 75, 77, 84, 86, 93, 95, 102, 104, 111, 113, 120, 122, 129, 131, 138, 140 -> type = BetType.QUADRUPLE_NUMBER;
		case 38, 40, 42, 44, 45, 47, 49, 51, 53, 54, 56, 58, 60, 62, 63, 65, 67, 69, 71, 72, 74, 76, 78, 80, 81, 83, 85, 87, 89, 90, 92, 94, 96, 98, 99, 101, 103, 105, 107, 108, 110, 112, 114, 116, 117, 119, 121, 123, 125, 126, 128, 130,
				132, 134, 135, 137, 139, 141, 143, 144 ->
			type = BetType.DOUBLE_NUMBER;
		case 39, 41, 43, 52, 61, 70, 79, 88, 97, 106, 115, 124, 133, 142 -> type = BetType.TRIPLE_NUMBER;
		case 46, 55, 64, 73, 82, 91, 100, 109, 118, 127, 136 -> type = BetType.SIX_NUMBER;
		case 145 -> type = BetType.FIRST_COLUMN;
		case 146 -> type = BetType.SECOND_COLUMN;
		case 147 -> type = BetType.THIRD_COLUMN;
		case 200 -> type = BetType.FIRST_DOZEN;
		case 201 -> type = BetType.SECOND_DOZEN;
		case 202 -> type = BetType.THIRD_DOZEN;
		case 203 -> type = BetType.FIRST_HALF;
		case 204 -> type = BetType.EVEN;
		case 205 -> type = BetType.RED;
		case 206 -> type = BetType.BLACK;
		case 207 -> type = BetType.ODD;
		case 208 -> type = BetType.SECOND_HALF;
		default -> throw new IllegalArgumentException("Non existing betPosition:" + betPosition);
		}
		return type;
	}

	private static void validateGivenParameter(Integer betPosition) {
		if (betPosition == null)
			throw new IllegalArgumentException("betPosition number missing");
	}
}