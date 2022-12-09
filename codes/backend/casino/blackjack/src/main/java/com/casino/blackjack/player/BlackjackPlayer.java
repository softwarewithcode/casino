package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.ISeatedTable;

public class BlackjackPlayer extends CasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackPlayer.class.getName());
	private static final BigDecimal INSURANCE_FACTOR = new BigDecimal("0.5");
	private List<IHand> hands;

	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, ISeatedTable table) {
		super(name, id, startBalance, table);
		hands = new ArrayList<IHand>();
		hands.add(createNewHand(true));
	}

	public boolean canTake() {
		IHand hand = getActiveHand();
		if (hand == null)
			return false;
		List<Integer> values = hand.calculateValues(); // Smallest value in 0 pos.
		return values.get(0) < 21;
	}

	public BlackjackHand createNewHand(boolean active) {
		return new BlackjackHand(UUID.randomUUID(), active);
	}

	public boolean hasActiveHand() {
		return hands.stream().filter(hand -> hand.isActive()).findAny().isPresent();
	}

	public boolean isInsuranceCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	public void splitStartingHand() {
		checkPlayerLock();
		validateSplitPreConditions();
		BetUtil.verifySufficentBalance(hands.get(0).getBet(), this); // Check balance after getting lock
		IHand splitHand = new BlackjackHand(UUID.randomUUID(), false);
		Card cardFromStartingHand = hands.get(0).getCards().remove(1);
		splitHand.addCard(cardFromStartingHand);
		splitHand.updateBet(hands.get(0).getBet());// immutable BigDecimal
		increaseTotalBet(splitHand.getBet());
		hands.add(splitHand);
	}

	public void stand() {
		IHand activeHand = getActiveHand();
		activeHand.stand();
		if (hasSecondHand(activeHand))
			activateSecondHand();
	}

	private boolean hasSecondHand(IHand activeHand) {
		return getHands().indexOf(activeHand) == 0 && getHands().size() == 2;
	}

	private void activateSecondHand() {
		getHands().get(1).activate();
	}

	public void doubleDown(Card ref) {
		try {
			checkPlayerLock();
			validateDoubleDownPreConditions();
			increaseTotalBet(getTotalBet());
			getFirstHand().doubleDown(ref);
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}
	}

	private void checkPlayerLock() {
		if (!isPlayerInTurnLocked())
			throw new ConcurrentModificationException("no playerLock acquired earlier");
	}

	public void insure() {
		try {
			checkPlayerLock();
			validateInsuringConditions();
			getFirstHand().insure();
			increaseTotalBet(getFirstHand().getBet().multiply(INSURANCE_FACTOR));
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}
	}

	private boolean isPlayerInTurnLocked() {
		return getPlayerLock().tryLock();
	}

	private void validateInsuringConditions() {
		validateActionConditions();
		if (getFirstHand().isInsured())
			throw new IllegalPlayerActionException("hand has been insured earlier ", 10);
		if (getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("cannot insure, hand has been doubled earlier ", 10);
		if (getFirstHand().isBlackjack())
			throw new IllegalPlayerActionException("cannot insure, hand is blackjack ", 10);
	}

	private void validateDoubleDownPreConditions() {
		validateActionConditions();
		if (getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("hand has been doubled before ", 10);
		if (getFirstHand().isBlackjack())
			throw new IllegalPlayerActionException("blacjack cannot be doubled ", 10);
		List<Integer> values = getFirstHand().calculateValues();
		int val = values.get(0);
		if (!(val >= 9 && val <= 11))
			throw new IllegalPlayerActionException("hand value does not allow doubling; " + getFirstHand().getCards().get(0) + " " + getFirstHand().getCards().get(1), 10);
	}

	private void validateSplitPreConditions() {
		validateActionConditions();
		if (!BlackjackUtil.haveSameValue(getFirstHand().getCards().get(0), getFirstHand().getCards().get(1)))
			throw new IllegalPlayerActionException("not equal values", 4);
		if (getFirstHand().isInsured())
			throw new IllegalPlayerActionException("cannot split insured hand", 4);
		if (getFirstHand().isDoubled())
			throw new IllegalPlayerActionException("hand has been doubled before ", 10);
	}

	private void validateActionConditions() {
		if (this.hands.size() != 1)
			throw new IllegalPlayerActionException("wrong hand count:" + getName() + " " + hands.size(), 1);
		if (!hands.get(0).isActive())
			throw new IllegalPlayerActionException("first hand is not active " + getName(), 2);
		if (hands.get(0).getCards().size() != 2)
			throw new IllegalPlayerActionException("starting hand does not contain exactly two cards:" + getName() + " " + hands.get(0).getCards(), 3);
	}

	public IHand getActiveHand() {
		return hands.stream().filter(hand -> hand.isActive()).findFirst().orElse(null);
	}

	public void addCard(IHand hand, Card card) {
		if (hand == null)
			throw new IllegalArgumentException("cannot add card to non existing hand");
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

	@Override
	public boolean canAct() {
		return getActiveHand() != null;
	}

	@Override
	public boolean hasWinningChance() {
		return hands.stream().filter(hand -> hand.hasWinningChance()).findAny().isPresent();
	}

	@Override
	public void reset() {
		super.reset();
		hands = new ArrayList<IHand>();
		// hands.add(createNewHand(true));
	}

	@Override
	public boolean isCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	public IHand getFirstHand() {
		return getHands().get(0);
	}
}
