package com.casino.poker.hand;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;

public class HandFactory {
    private static final Integer CARDS_IN_HOLDEM_HAND = 5;

    // TODO refactor and combine and refactor
    public static HoldemHand constructPokerHand(List<Card> tableCards, List<Card> holeCards) {
        verifySize(tableCards, holeCards);
        // In texas hold'em lists can be directly combined. In variants like omaha/omaha5 should check combinations of seven cards
        List<Card> combinedCards = new ArrayList<>();
        combinedCards.addAll(tableCards);
        combinedCards.addAll(holeCards);
        combinedCards = Collections.unmodifiableList(combinedCards);
        Optional<HoldemHand> handOptional = checkStraightFlush(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkFourOfKind(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkFullHouse(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkFlush(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkStraight(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkThreeOfKind(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkTwoPairs(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        handOptional = checkPair(combinedCards);
        if (handOptional.isPresent())
            return handOptional.get();
        List<Card> copy = new ArrayList<>(combinedCards);
        List<Card> highCards = fillWithHighCards(new ArrayList<>(), copy);
        return new HoldemHand(highCards, PokerHandType.HIGH_CARD);
    }

    private static Optional<HoldemHand> checkPair(List<Card> combinedCards) {
        Optional<Integer> firstPair = getHighestRankWithFrequencyOf(combinedCards, 2);
        if (firstPair.isEmpty())
            return Optional.empty();
        int firstPairRank = firstPair.get();
        List<Card> pairList = combinedCards.stream().filter(card -> card.getRank() == firstPairRank).collect(Collectors.toList());
        List<Card> highCardList = combinedCards.stream().filter(card -> card.getRank() != firstPairRank).collect(Collectors.toList());
        HoldemHand hand = new HoldemHand(fillWithHighCards(pairList, highCardList), PokerHandType.PAIR);
        return Optional.of(hand);
    }

    private static Optional<HoldemHand> checkTwoPairs(List<Card> combinedCards) {
        Optional<Integer> firstPair = getHighestRankWithFrequencyOf(combinedCards, 2);
        if (firstPair.isEmpty())
            return Optional.empty();
        int firstPairRank = firstPair.get();
        List<Card> biggestPairList = combinedCards.stream().filter(card -> card.getRank() == firstPairRank).collect(Collectors.toList());
        List<Card> secondPairList = combinedCards.stream().filter(card -> card.getRank() != firstPairRank).collect(Collectors.toList());
        Optional<Integer> secondPair = getHighestRankWithFrequencyOf(secondPairList, 2);
        if (secondPair.isEmpty())
            return Optional.empty();
        int secondPairRank = secondPair.get();
        List<Card> secondPairCards = combinedCards.stream().filter(card -> card.getRank() == secondPairRank).toList();
        biggestPairList.addAll(secondPairCards);
        List<Card> highCardList = combinedCards.stream().filter(card -> card.getRank() != secondPairRank && card.getRank() != firstPairRank).collect(Collectors.toList());
        List<Card> twoPairs = moveBiggestPairFirst(biggestPairList);
        HoldemHand hand = new HoldemHand(fillWithHighCards(twoPairs, highCardList), PokerHandType.TWO_PAIRS);
        return Optional.of(hand);
    }

    private static Optional<HoldemHand> checkThreeOfKind(List<Card> combinedCards) {
        Optional<Integer> threeOfKind = getHighestRankWithFrequencyOf(combinedCards, 3);
        if (threeOfKind.isEmpty())
            return Optional.empty();
        int threeOfKindRank = threeOfKind.get();
        List<Card> threeOfKindsList = combinedCards.stream().filter(card -> card.getRank() == threeOfKindRank).collect(Collectors.toList());
        List<Card> highCardList = combinedCards.stream().filter(card -> card.getRank() != threeOfKindRank).collect(Collectors.toList());
        HoldemHand hand = new HoldemHand(fillWithHighCards(threeOfKindsList, highCardList), PokerHandType.THREE_OF_KIND);
        return Optional.of(hand);
    }

    private static Optional<HoldemHand> checkFlush(List<Card> combinedCards) {
        List<Card> flush = findAllFlushCardsSorted(combinedCards);
        if (flush.size() < 5)
            return Optional.empty();
        HoldemHand hand = new HoldemHand(flush.subList(0, 5), PokerHandType.FLUSH);
        return Optional.of(hand);
    }

    private static Optional<HoldemHand> checkFullHouse(List<Card> combinedCards) {
        Optional<Integer> threeOfKind = getHighestRankWithFrequencyOf(combinedCards, 3);
        if (threeOfKind.isEmpty())
            return Optional.empty();
        int threeOfKindRank = threeOfKind.get();
        List<Card> threeOfKindsList = combinedCards.stream().filter(card -> card.getRank() == threeOfKindRank).collect(Collectors.toList());
        List<Card> potentialPairs = combinedCards.stream().filter(card -> card.getRank() != threeOfKindRank).collect(Collectors.toList());
        Optional<Integer> pair = getHighestRankWithFrequencyOf(potentialPairs, 2);
        if (pair.isEmpty())
            return Optional.empty();
        Integer pairRank = pair.get();
        List<Card> pairs = combinedCards.stream().filter(card -> card.getRank() == pairRank).toList();
        threeOfKindsList.addAll(pairs);
        HoldemHand hand = new HoldemHand(threeOfKindsList, PokerHandType.FULL_HOUSE);
        return Optional.of(hand);
    }

    private static void addAcesAsHighCards(List<Card> combinedCards) {
        List<Card> aces = combinedCards.stream().filter(card -> card.getRank() == 1).toList();
        if (aces.isEmpty())
            return;
        aces.forEach(ace -> combinedCards.add(Card.of(14, ace.getSuit())));
    }

    private static Optional<HoldemHand> checkFourOfKind(List<Card> combinedCards) {
        Optional<Integer> fourSimilarRanks = getHighestRankWithFrequencyOf(combinedCards, 4);
        if (fourSimilarRanks.isEmpty())
            return Optional.empty();
        int rank = fourSimilarRanks.get();
        List<Card> fourOfKind = combinedCards.stream().filter(card -> card.getRank() == rank).collect(Collectors.toList());
        List<Card> highCardList = combinedCards.stream().filter(card -> card.getRank() != rank).collect(Collectors.toList());
        HoldemHand hand = new HoldemHand(fillWithHighCards(fourOfKind, highCardList), PokerHandType.FOUR_OF_KIND);
        return Optional.of(hand);
    }

    private static List<Card> fillWithHighCards(List<Card> listToBeFilled, List<Card> listToCheckFrom) {
        int howMany = CARDS_IN_HOLDEM_HAND - listToBeFilled.size();
        addAcesAsHighCards(listToCheckFrom);
        List<Card> sortedLimited = sortDistinctByRank(listToCheckFrom).stream().limit(howMany).toList();
        listToBeFilled.addAll(sortedLimited);
        return listToBeFilled;
    }

    private static Optional<Integer> getHighestRankWithFrequencyOf(List<Card> cards, Integer frequency) {
        Map<Integer, Integer> frequencyMap = calculateRankFrequencies(cards);
        List<Integer> sortedFrequencies = frequencyMap.entrySet().stream().filter(entry -> entry.getValue().equals(frequency)).map(Map.Entry::getKey).toList();
        if (sortedFrequencies.isEmpty())
            return Optional.empty();
        Optional<Integer> p = sortedFrequencies.stream().filter(rank -> rank == 1).findFirst();
        if (p.isPresent())
            return Optional.of(1);
        return Optional.ofNullable(sortedFrequencies.get(sortedFrequencies.size() - 1));
    }

    private static Map<Integer, Integer> calculateRankFrequencies(List<Card> cards) {
        return cards.stream().map(Card::getRank).collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(x -> 1)));
    }

    private static void verifySize(List<Card> tableCards, List<Card> holeCards) {
        if (tableCards.size() != 5)
            throw new IllegalArgumentException("Expected 5 cards in Texas hold'em, was " + tableCards.size());
        if (holeCards.size() < 2 || holeCards.size() > 5) // Omaha5 has 5
            throw new IllegalArgumentException("Wrong private card amount: was" + holeCards.size());
    }

    private static Optional<HoldemHand> checkStraightFlush(List<Card> cards) {
        List<Card> flushCards = findAllFlushCardsSorted(cards);
        if (flushCards.isEmpty())
            return Optional.empty();
        Optional<HoldemHand> handOptional = checkStraight(flushCards);
        handOptional.ifPresent(holdemHand -> holdemHand.setType(PokerHandType.STRAIGHT_FLUSH));
        return handOptional;
    }

    private static Optional<HoldemHand> checkStraight(List<Card> cards) {
        List<Card> distinctSortedCards = sortDistinctByRank(cards);
        Optional<Card> aceOptional = distinctSortedCards.stream().filter(Card::isAce).findAny();
        if (aceOptional.isEmpty())
            return findStraight(distinctSortedCards);
        if (distinctSortedCards.get(0).getRank() != 14)
            distinctSortedCards.add(0, Card.of(14, aceOptional.get().getSuit()));
        return findStraight(distinctSortedCards);
    }

    private static Optional<HoldemHand> findStraight(List<Card> distinctSortedCards) {
        int comparisonRank = 0;
        List<Card> straight = new ArrayList<>();
        for (Card card : distinctSortedCards) {
            if (comparisonRank - card.getRank() != 1 && !straight.isEmpty()) {
                straight.clear();
            }
            straight.add(card);
            comparisonRank = card.getRank();
            if (straight.size() == 5)
                break;
        }
        HoldemHand hand = null;
        if (straight.size() == 5) {
            hand = new HoldemHand(straight, PokerHandType.STRAIGHT);
        }
        return Optional.ofNullable(hand);
    }

    private static List<Card> findAllFlushCardsSorted(List<Card> cards) {
        Map<Suit, List<Card>> cardsBySuit = cards.stream().collect(Collectors.groupingBy(Card::getSuit));
        cardsBySuit.values().removeIf(list -> list.size() < 5);
        Optional<List<Card>> flush = cardsBySuit.values().stream().findFirst();
        if (flush.isEmpty()) {
            return Collections.emptyList();
        }
        List<Card> flushCards = flush.get();
        Optional<Card> aceOptional = flush.get().stream().filter(Card::isAce).findAny();
        if (aceOptional.isPresent())
            flushCards.add(Card.of(14, flushCards.get(0).getSuit()));
        return sortDistinctByRank(flush.get());
    }

    private static List<Card> sortDistinctByRank(List<Card> cards) {
        return cards.stream().sorted(Comparator.comparing(Card::getRank).reversed()).distinct().collect(Collectors.toList());
    }

    private static List<Card> sortByRank(List<Card> cards) {
        return cards.stream().sorted(Comparator.comparing(Card::getRank).reversed()).collect(Collectors.toList());
    }

    private static List<Card> moveBiggestPairFirst(List<Card> pairs) {
        Optional<Card> aceOptional = pairs.stream().filter(Card::isAce).findFirst();
        if (aceOptional.isPresent())
            return pairs.stream().sorted(Comparator.comparing(Card::getRank)).collect(Collectors.toList());
        return sortByRank(pairs);
    }

}
