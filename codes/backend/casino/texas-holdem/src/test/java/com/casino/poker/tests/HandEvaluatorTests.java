package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.poker.hand.HandFactory;
import com.casino.poker.hand.HoldemHand;

public class HandEvaluatorTests {

	@Test
	public void royalFlushesEqualsDraw() {
		List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(11, Suit.DIAMOND), Card.of(12, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
		List<Card> holeCardsPlayer1 = List.of(Card.of(1, Suit.SPADE), Card.of(2, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(3, Suit.SPADE), Card.of(4, Suit.DIAMOND));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCardsPlayer1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(0, hand1.compareTo(hand2));
	}

	@Test
	public void royalFlushesWinsStraightFlush() {
		List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(11, Suit.DIAMOND), Card.of(12, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(5, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.DIAMOND), Card.of(2, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(9, Suit.SPADE), Card.of(4, Suit.DIAMOND));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void straightFlushWinsStraightFlush() {
		List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(4, Suit.DIAMOND), Card.of(5, Suit.DIAMOND), Card.of(2, Suit.DIAMOND), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.DIAMOND), Card.of(2, Suit.SPADE));
		List<Card> holeCards2 = List.of(Card.of(9, Suit.SPADE), Card.of(6, Suit.DIAMOND));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void straightFlushWinsFullHouse() {
		List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(10, Suit.CLUB), Card.of(5, Suit.DIAMOND), Card.of(3, Suit.DIAMOND), Card.of(4, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.DIAMOND), Card.of(2, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(3, Suit.SPADE), Card.of(3, Suit.CLUB));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void straightFlushWinsAceHighFlush() {
		List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(9, Suit.DIAMOND), Card.of(5, Suit.DIAMOND), Card.of(4, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(2, Suit.DIAMOND), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(13, Suit.SPADE), Card.of(12, Suit.CLUB));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards2);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards1);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void straightFlushWinsStraight() {
		List<Card> tableCards = List.of(Card.of(10, Suit.CLUB), Card.of(6, Suit.DIAMOND), Card.of(5, Suit.DIAMOND), Card.of(4, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(2, Suit.DIAMOND), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(7, Suit.SPADE), Card.of(8, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void fourOfKindFromTableEqualsDraw() {
		List<Card> tableCards = List.of(Card.of(4, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(4, Suit.SPADE), Card.of(4, Suit.HEART), Card.of(1, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(13, Suit.DIAMOND), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(12, Suit.SPADE), Card.of(8, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(0, hand1.compareTo(hand2));
	}

	@Test
	public void fourOfKindWinsFourOfKind() {
		List<Card> tableCards = List.of(Card.of(4, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(4, Suit.SPADE), Card.of(5, Suit.HEART), Card.of(5, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(4, Suit.HEART), Card.of(3, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(5, Suit.SPADE), Card.of(5, Suit.CLUB));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void fullHouseWinsStraight() {
		List<Card> tableCards = List.of(Card.of(4, Suit.CLUB), Card.of(3, Suit.DIAMOND), Card.of(4, Suit.SPADE), Card.of(7, Suit.HEART), Card.of(8, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(4, Suit.HEART), Card.of(3, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(5, Suit.SPADE), Card.of(6, Suit.CLUB));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void aceHighFlushWinsKingHighFlush() {
		List<Card> tableCards = List.of(Card.of(4, Suit.CLUB), Card.of(3, Suit.CLUB), Card.of(6, Suit.CLUB), Card.of(12, Suit.CLUB), Card.of(8, Suit.DIAMOND));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.CLUB), Card.of(9, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(13, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void ninesFullOfEightsWinsNinesFullOfSevens() {
		List<Card> tableCards = List.of(Card.of(9, Suit.CLUB), Card.of(9, Suit.SPADE), Card.of(9, Suit.HEART), Card.of(12, Suit.CLUB), Card.of(11, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(8, Suit.CLUB), Card.of(8, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void flushWinsFlushWithHigherPrivateCard() {
		List<Card> tableCards = List.of(Card.of(1, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(9, Suit.CLUB), Card.of(12, Suit.CLUB), Card.of(8, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(8, Suit.CLUB), Card.of(2, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(7, Suit.CLUB), Card.of(3, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void threeOfKindsFromTableEqualsDraw() {
		List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(7, Suit.CLUB), Card.of(11, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(10, Suit.CLUB), Card.of(2, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(9, Suit.CLUB), Card.of(3, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(0, hand1.compareTo(hand2));
	}

	@Test
	public void threeOfKindsWithAceHighWins() {
		List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(7, Suit.CLUB), Card.of(11, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(13, Suit.CLUB), Card.of(4, Suit.HEART));
		List<Card> holeCards2 = List.of(Card.of(1, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void threeOfKindsWithTenHighWins() {
		List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(7, Suit.CLUB), Card.of(3, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(9, Suit.CLUB), Card.of(4, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(10, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void twoPairsFromTableEqualsDraws() {
		List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(4, Suit.CLUB), Card.of(1, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(2, Suit.CLUB), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(13, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(0, hand1.compareTo(hand2));
	}

	@Test
	public void twoPairsWithAceHighWins() {
		List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(7, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(4, Suit.CLUB), Card.of(3, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.CLUB), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(13, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void aceHighCardWinsKingHighCard() {
		List<Card> tableCards = List.of(Card.of(9, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(8, Suit.CLUB), Card.of(7, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.CLUB), Card.of(3, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(13, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void pairOfAcesWinsPairOfQueens() {
		List<Card> tableCards = List.of(Card.of(9, Suit.CLUB), Card.of(4, Suit.SPADE), Card.of(12, Suit.DIAMOND), Card.of(8, Suit.CLUB), Card.of(7, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(1, Suit.CLUB), Card.of(1, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(12, Suit.CLUB), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(-1, hand1.compareTo(hand2));
	}

	@Test
	public void pairOfQueensLosesToPairOfAces() {
		List<Card> tableCards = List.of(Card.of(9, Suit.CLUB), Card.of(4, Suit.SPADE), Card.of(12, Suit.DIAMOND), Card.of(8, Suit.CLUB), Card.of(7, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(12, Suit.CLUB), Card.of(6, Suit.SPADE));
		List<Card> holeCards2 = List.of(Card.of(1, Suit.CLUB), Card.of(1, Suit.DIAMOND));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}

	@Test
	public void highCardsFromTableEqualsDraw() {
		List<Card> tableCards = List.of(Card.of(1, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(9, Suit.CLUB), Card.of(8, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(7, Suit.CLUB), Card.of(6, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(7, Suit.DIAMOND), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(0, hand1.compareTo(hand2));
	}

	@Test
	public void highCardsWins() {
		List<Card> tableCards = List.of(Card.of(1, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(12, Suit.DIAMOND), Card.of(9, Suit.CLUB), Card.of(2, Suit.HEART));
		List<Card> holeCards1 = List.of(Card.of(5, Suit.CLUB), Card.of(6, Suit.DIAMOND));
		List<Card> holeCards2 = List.of(Card.of(7, Suit.DIAMOND), Card.of(6, Suit.SPADE));
		HoldemHand hand1 = HandFactory.constructPokerHand(tableCards, holeCards1);
		HoldemHand hand2 = HandFactory.constructPokerHand(tableCards, holeCards2);
		assertEquals(1, hand1.compareTo(hand2));
	}
}
