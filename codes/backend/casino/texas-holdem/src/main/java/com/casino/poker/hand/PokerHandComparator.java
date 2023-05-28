package com.casino.poker.hand;

public class PokerHandComparator {

    public static int compare(PokerHand o1, PokerHand other) {
        if (other == null)
            return -1;
        int myHandValue = o1.getType().getValue();
        int otherValue = other.getType().getValue();
        if (myHandValue > otherValue)
            return -1;
        if (otherValue > myHandValue)
            return 1;
        PokerHandType type = o1.getType();
        int myMostSignificantRank = o1.getMostSignificantCard().getRank();
        int otherMostSignificantRank = other.getMostSignificantCard().getRank();
        // ace vs. 5 in low straight
        if (isEnoughToCompareMostSignificantCards(type)) {
            return compareMostSignificantCard(myMostSignificantRank, otherMostSignificantRank);
        }
        return compareMostSignificantCardAndSumOfCards(o1, other, myMostSignificantRank, otherMostSignificantRank);
    }

    private static boolean isEnoughToCompareMostSignificantCards(PokerHandType type) {
        return type == PokerHandType.STRAIGHT_FLUSH || type == PokerHandType.STRAIGHT || type == PokerHandType.FOUR_OF_KIND;
    }

    private static int compareMostSignificantCard(int myMostSignificantRank, int otherMostSignificantRank) {
        if (myMostSignificantRank == otherMostSignificantRank)
            return 0;
        return myMostSignificantRank > otherMostSignificantRank ? -1 : 1;
    }

    private static int compareMostSignificantCardAndSumOfCards(PokerHand o1, PokerHand other, int myMostSignificantRank, int otherMostSignificantRank) {
        if (myMostSignificantRank == otherMostSignificantRank)
            return compareCardsSum(o1, other);
        if (myMostSignificantRank == 1)
            myMostSignificantRank = 14;
        if (otherMostSignificantRank == 1)
            otherMostSignificantRank = 14;
        return myMostSignificantRank > otherMostSignificantRank ? -1 : 1;
    }

    private static int compareCardsSum(PokerHand hand1, PokerHand hand2) {
        int sum1 = hand1.calculateCardsSum();
        int sum2 = hand2.calculateCardsSum();
        return compareMostSignificantCard(sum1, sum2);
    }
}
