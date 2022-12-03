package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalDoublingException;
import com.casino.common.exception.IllegalSplitException;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.ISeatedTable;

public class BlackjackPlayer extends CasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackPlayer.class.getName());
	private List<IHand> hands;

	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, ISeatedTable table) {
		super(name, id, startBalance, table);
		hands = new ArrayList<IHand>();
		IHand hand = createNewHand(true);
		// hand.activate();
		hands.add(hand);
	}

	public boolean canTake() {
		IHand hand = getActiveHand();
		List<Integer> values = hand.calculateValues(); // Smallest value in 0 pos.
		return values.get(0) < 21 && !hand.isBlackjack();
	}

	public BlackjackHand createNewHand(boolean active) {
		return new BlackjackHand(UUID.randomUUID(), active);
	}

	public void splitStartingHand() {
		validateSplitConditions();
		IHand splitHand = new BlackjackHand(UUID.randomUUID(), false);
		Card cardFromStartingHand = hands.get(0).getCards().remove(1);
		splitHand.addCard(cardFromStartingHand);
		hands.add(splitHand);
	}

	public void stand() {
		getActiveHand().complete();
	}

	public void doubleDown() {
		try {
			if (!getPlayerLock().tryLock())
				throw new ConcurrentModificationException("no balance lock acquired");
			increaseBet(getBet());
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.SEVERE, "Doubling failed ", e);
			throw new IllegalDoublingException("insufficent funds", 1);
		} catch (ConcurrentModificationException ce) {
			LOGGER.log(Level.SEVERE, "Doubling failed ", ce);
			throw new IllegalDoublingException("balance handling is reserved", 2);
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}
	}

	private void validateSplitConditions() {
		if (this.hands.size() != 1)
			throw new IllegalSplitException("wrong hand count:" + hands.size(), 1);
		if (!hands.get(0).isActive())
			throw new IllegalSplitException("first hand is not active", 2);
		if (hands.get(0).getCards().size() != 2)
			throw new IllegalSplitException("starting hand does not contain exactly two cards:" + hands.get(0).getCards(), 3);
		if (!BlackjackUtil.haveSameValue(hands.get(0).getCards().get(0), hands.get(0).getCards().get(1)))
			throw new IllegalSplitException("not equal values", 4);
	}

	public IHand getActiveHand() {
		return hands.stream().filter(hand -> !hand.isCompleted()).findFirst().orElse(null);
	}

	public void addCard(IHand hand, Card card) {
		if (hand == null)
			throw new IllegalArgumentException("cannot add card to non existing hand");
		if (!hand.isActive())
			throw new IllegalArgumentException("hand is not active");
		hand.addCard(card);
	}

	public void clearHands() {
		hands.clear();
	}

	@Override
	public List<IHand> getHands() {
		return hands;
	}

	@Override
	public String toString() {
		return "[name=" + getName() + ", id=" + getId() + ", hands=" + hands + "]";
	}

}
