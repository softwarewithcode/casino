package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.poker.hand.HandFactory;
import com.casino.poker.hand.HoldemHand;
import com.casino.poker.hand.PokerHandType;

public class HandCreatorTests {

    @Test
    public void royalFlushIsFoundFromTableCards() {
        List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(11, Suit.DIAMOND), Card.of(12, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
        List<Card> holeCards = List.of(Card.of(1, Suit.SPADE), Card.of(2, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, holeCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void royalFlushIsFoundFromTableCardsAndPrivateCard() {
        List<Card> tableCards = List.of(Card.of(2, Suit.CLUB), Card.of(10, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(12, Suit.CLUB), Card.of(13, Suit.CLUB));
        List<Card> holeCards = List.of(Card.of(1, Suit.CLUB), Card.of(1, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, holeCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void royalFlushIsFoundFromTableCardsAndHoleCards() {
        List<Card> tableCards = List.of(Card.of(2, Suit.SPADE), Card.of(10, Suit.DIAMOND), Card.of(12, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(5, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(1, Suit.DIAMOND), Card.of(11, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void lowStraightFlushIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.HEART), Card.of(4, Suit.HEART), Card.of(3, Suit.HEART), Card.of(13, Suit.HEART), Card.of(5, Suit.HEART));
        List<Card> privateCards = List.of(Card.of(1, Suit.HEART), Card.of(2, Suit.HEART));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(5, finalHand.getMostSignificantCard().getRank());
    }

    @Test
    public void middleSraightFlushIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.HEART), Card.of(8, Suit.SPADE), Card.of(5, Suit.SPADE), Card.of(4, Suit.SPADE), Card.of(7, Suit.SPADE));
        List<Card> privateCards = List.of(Card.of(2, Suit.CLUB), Card.of(6, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(8, finalHand.getMostSignificantCard().getRank());
    }

    @Test
    public void highestStraightFlushIsFound() {
        List<Card> tableCards = List.of(Card.of(11, Suit.SPADE), Card.of(6, Suit.SPADE), Card.of(9, Suit.SPADE), Card.of(8, Suit.SPADE), Card.of(7, Suit.SPADE));
        List<Card> privateCards = List.of(Card.of(12, Suit.SPADE), Card.of(10, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT_FLUSH, finalHand.getType());
        assertEquals(12, finalHand.getMostSignificantCard().getRank());
    }

    @Test
    public void fourOfKindIsFoundFromTable() {
        List<Card> tableCards = List.of(Card.of(9, Suit.SPADE), Card.of(9, Suit.CLUB), Card.of(9, Suit.DIAMOND), Card.of(9, Suit.HEART), Card.of(5, Suit.SPADE));
        List<Card> privateCards = List.of(Card.of(6, Suit.SPADE), Card.of(1, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FOUR_OF_KIND, finalHand.getType());
        assertEquals(9, finalHand.getMostSignificantCard().getRank());
        assertEquals(14, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void fourOfKindIsFoundFromTableAndPrivateCards() {
        List<Card> tableCards = List.of(Card.of(9, Suit.SPADE), Card.of(2, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(9, Suit.HEART), Card.of(5, Suit.SPADE));
        List<Card> privateCards = List.of(Card.of(9, Suit.DIAMOND), Card.of(9, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FOUR_OF_KIND, finalHand.getType());
        assertEquals(9, finalHand.getMostSignificantCard().getRank());
        assertEquals(5, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void acesFourKindIsFoundWith9() {
        List<Card> tableCards = List.of(Card.of(1, Suit.SPADE), Card.of(1, Suit.CLUB), Card.of(4, Suit.DIAMOND), Card.of(9, Suit.HEART), Card.of(1, Suit.HEART));
        List<Card> privateCards = List.of(Card.of(9, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FOUR_OF_KIND, finalHand.getType());
        assertEquals(1, finalHand.getMostSignificantCard().getRank());
        assertEquals(9, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void sevensFullOfTwosIsFound() {
        List<Card> tableCards = List.of(Card.of(7, Suit.CLUB), Card.of(11, Suit.CLUB), Card.of(7, Suit.DIAMOND), Card.of(2, Suit.CLUB), Card.of(13, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(7, Suit.CLUB), Card.of(2, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FULL_HOUSE, finalHand.getType());
        assertEquals(7, finalHand.getMostSignificantCard().getRank());
        assertEquals(7, finalHand.getCards().get(2).getRank());
        assertEquals(2, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void acesFullOfKingsIsFound() {
        List<Card> tableCards = List.of(Card.of(1, Suit.CLUB), Card.of(13, Suit.CLUB), Card.of(1, Suit.DIAMOND), Card.of(2, Suit.CLUB), Card.of(10, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(1, Suit.SPADE), Card.of(13, Suit.HEART));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FULL_HOUSE, finalHand.getType());
        assertEquals(1, finalHand.getMostSignificantCard().getRank());
        assertEquals(1, finalHand.getCards().get(2).getRank());
        assertEquals(13, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void kingsFullOfAcesIsFound() {
        List<Card> tableCards = List.of(Card.of(13, Suit.CLUB), Card.of(1, Suit.CLUB), Card.of(13, Suit.DIAMOND), Card.of(2, Suit.CLUB), Card.of(10, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(13, Suit.SPADE), Card.of(1, Suit.HEART));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FULL_HOUSE, finalHand.getType());
        assertEquals(13, finalHand.getMostSignificantCard().getRank());
        assertEquals(13, finalHand.getCards().get(2).getRank());
        assertEquals(1, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void biggestFullHouseIsFound() {
        List<Card> tableCards = List.of(Card.of(13, Suit.CLUB), Card.of(13, Suit.HEART), Card.of(13, Suit.DIAMOND), Card.of(10, Suit.DIAMOND), Card.of(10, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(11, Suit.SPADE), Card.of(11, Suit.HEART));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FULL_HOUSE, finalHand.getType());
        assertEquals(13, finalHand.getMostSignificantCard().getRank());
        assertEquals(11, finalHand.getCards().get(3).getRank());
    }

    @Test
    public void biggestFullHouseIsFound2() {
        List<Card> tableCards = List.of(Card.of(13, Suit.HEART), Card.of(13, Suit.DIAMOND), Card.of(11, Suit.DIAMOND), Card.of(10, Suit.CLUB), Card.of(13, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(10, Suit.SPADE), Card.of(11, Suit.HEART));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FULL_HOUSE, finalHand.getType());
        assertEquals(13, finalHand.getMostSignificantCard().getRank());
        assertEquals(11, finalHand.getCards().get(3).getRank());
    }

    @Test
    public void flushIsFoundWithAceHigh() {
        List<Card> tableCards = List.of(Card.of(10, Suit.CLUB), Card.of(11, Suit.DIAMOND), Card.of(12, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(5, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(1, Suit.DIAMOND), Card.of(2, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FLUSH, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void flushIsFoundWithTenHigh() {
        List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(9, Suit.SPADE), Card.of(8, Suit.DIAMOND), Card.of(7, Suit.DIAMOND), Card.of(6, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(3, Suit.DIAMOND), Card.of(2, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.FLUSH, finalHand.getType());
        assertEquals(10, finalHand.getMostSignificantCard().getRank());
    }

    @Test
    public void tenHighStraightIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.CLUB), Card.of(9, Suit.SPADE), Card.of(8, Suit.DIAMOND), Card.of(7, Suit.DIAMOND), Card.of(6, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(3, Suit.DIAMOND), Card.of(2, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT, finalHand.getType());
        assertEquals(10, finalHand.getMostSignificantCard().getRank());
    }

    @Test
    public void aceHighStraightIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(11, Suit.CLUB), Card.of(12, Suit.HEART), Card.of(13, Suit.CLUB), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(8, Suit.DIAMOND), Card.of(1, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void fiveHighStraightIsFound() {
        List<Card> tableCards = List.of(Card.of(5, Suit.DIAMOND), Card.of(11, Suit.CLUB), Card.of(1, Suit.DIAMOND), Card.of(13, Suit.CLUB), Card.of(4, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(2, Suit.DIAMOND), Card.of(3, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT, finalHand.getType());
        assertEquals(5, finalHand.getMostSignificantCard().getRank());
        assertFalse(finalHand.getMostSignificantCard().isAce());
    }

    @Test
    public void sevenHighStraightIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.DIAMOND), Card.of(11, Suit.CLUB), Card.of(5, Suit.DIAMOND), Card.of(6, Suit.CLUB), Card.of(7, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(4, Suit.DIAMOND), Card.of(3, Suit.SPADE));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.STRAIGHT, finalHand.getType());
        assertEquals(7, finalHand.getCards().get(0).getRank());
        assertEquals(7, finalHand.getMostSignificantCard().getRank());
        assertEquals(6, finalHand.getCards().get(1).getRank());
        assertEquals(5, finalHand.getCards().get(2).getRank());
        assertEquals(4, finalHand.getCards().get(3).getRank());
        assertEquals(3, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void threeOfKindWithKingHighIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.SPADE), Card.of(4, Suit.HEART), Card.of(4, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(5, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(4, Suit.DIAMOND), Card.of(2, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.THREE_OF_KIND, finalHand.getType());
        assertEquals(4, finalHand.getMostSignificantCard().getRank());
        assertEquals(13, finalHand.getCards().get(3).getRank());
        assertEquals(10, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void threeOfKindWithAceHighIsFound() {
        List<Card> tableCards = List.of(Card.of(10, Suit.SPADE), Card.of(4, Suit.HEART), Card.of(4, Suit.DIAMOND), Card.of(1, Suit.DIAMOND), Card.of(5, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(4, Suit.DIAMOND), Card.of(2, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.THREE_OF_KIND, finalHand.getType());
        assertEquals(4, finalHand.getMostSignificantCard().getRank());
        assertEquals(14, finalHand.getCards().get(3).getRank());
        assertEquals(10, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void foursAndNinesAreFoundAsTwoPair() {
        List<Card> tableCards = List.of(Card.of(4, Suit.SPADE), Card.of(11, Suit.HEART), Card.of(9, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(4, Suit.DIAMOND), Card.of(2, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.TWO_PAIRS, finalHand.getType());
        assertEquals(9, finalHand.getMostSignificantCard().getRank());
        assertEquals(13, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void acesAndKingsAreFoundAsTwoPairsAmongstThreePairs() {
        List<Card> tableCards = List.of(Card.of(9, Suit.SPADE), Card.of(2, Suit.HEART), Card.of(1, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(1, Suit.DIAMOND), Card.of(13, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.TWO_PAIRS, finalHand.getType());
        assertTrue(finalHand.getMostSignificantCard().isAce());
        assertEquals(13, finalHand.getCards().get(2).getRank());
        assertEquals(9, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void pairOfFivesIsFound() {
        List<Card> tableCards = List.of(Card.of(1, Suit.SPADE), Card.of(5, Suit.HEART), Card.of(4, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(5, Suit.DIAMOND), Card.of(2, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.PAIR, finalHand.getType());
        assertEquals(5, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getCards().get(2).isAce());
    }

    @Test
    public void pairOfAcesFromTableIsFound() {
        List<Card> tableCards = List.of(Card.of(1, Suit.SPADE), Card.of(5, Suit.HEART), Card.of(4, Suit.DIAMOND), Card.of(1, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(2, Suit.DIAMOND), Card.of(8, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.PAIR, finalHand.getType());
        assertTrue(finalHand.getMostSignificantCard().isAce());
        assertEquals(9, finalHand.getCards().get(2).getRank());
        assertEquals(8, finalHand.getCards().get(3).getRank());
        assertEquals(5, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void pairOfAcesFromPrivateCardsIsFound() {
        List<Card> tableCards = List.of(Card.of(12, Suit.SPADE), Card.of(5, Suit.HEART), Card.of(4, Suit.DIAMOND), Card.of(13, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        List<Card> privateCards = List.of(Card.of(1, Suit.DIAMOND), Card.of(1, Suit.CLUB));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.PAIR, finalHand.getType());
        assertTrue(finalHand.getMostSignificantCard().isAce());
        assertTrue(finalHand.getCards().get(1).isAce());
        assertEquals(13, finalHand.getCards().get(2).getRank());
        assertEquals(12, finalHand.getCards().get(3).getRank());
        assertEquals(9, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void highCardIsFound() {
        List<Card> tableCards = List.of(Card.of(7, Suit.DIAMOND), Card.of(8, Suit.SPADE), Card.of(10, Suit.CLUB), Card.of(11, Suit.SPADE), Card.of(12, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(1, Suit.DIAMOND), Card.of(6, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.HIGH_CARD, finalHand.getType());
        assertEquals(14, finalHand.getMostSignificantCard().getRank());
        assertTrue(finalHand.getCards().get(0).isAce());
        assertEquals(12, finalHand.getCards().get(1).getRank());
        assertEquals(11, finalHand.getCards().get(2).getRank());
        assertEquals(10, finalHand.getCards().get(3).getRank());
        assertEquals(8, finalHand.getCards().get(4).getRank());
    }

    @Test
    public void nineHighIsFound() {
        List<Card> tableCards = List.of(Card.of(2, Suit.DIAMOND), Card.of(3, Suit.SPADE), Card.of(4, Suit.CLUB), Card.of(5, Suit.SPADE), Card.of(7, Suit.CLUB));
        List<Card> privateCards = List.of(Card.of(8, Suit.DIAMOND), Card.of(9, Suit.DIAMOND));
        HoldemHand finalHand = HandFactory.constructPokerHand(tableCards, privateCards);
        assertEquals(PokerHandType.HIGH_CARD, finalHand.getType());
        assertEquals(9, finalHand.getMostSignificantCard().getRank());
        assertEquals(8, finalHand.getCards().get(1).getRank());
        assertEquals(7, finalHand.getCards().get(2).getRank());
        assertEquals(5, finalHand.getCards().get(3).getRank());
        assertEquals(4, finalHand.getCards().get(4).getRank());
    }

}
