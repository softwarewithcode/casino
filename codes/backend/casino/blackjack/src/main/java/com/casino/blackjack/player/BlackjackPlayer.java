package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.ICasinoTable;
import com.casino.common.table.ISeatedTable;
import com.casino.common.user.Action;

public class BlackjackPlayer extends CasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackPlayer.class.getName());
	private List<IHand> hands;
	private List<Action> actions;

	public BlackjackPlayer(String name, UUID id, BigDecimal startBalance, ISeatedTable table) {
		super(name, id, startBalance, table);
		hands = new ArrayList<IHand>();
		hands.add(createNewHand(true));
		actions = new ArrayList<>(4);
	}

	public boolean canTake() {
		IHand hand = getActiveHand();
		if (hand == null)
			return false;
		List<Integer> values = hand.calculateValues(); // Smallest value in 0 pos.
		return values.get(0) < 21;
	}

	private BlackjackHand createNewHand(boolean active) {
		return new BlackjackHand(UUID.randomUUID(), active);
	}

	public void updateActions() {
		try {
			tryTakingPlayerLock();
			actions = new ArrayList<>();
			if (!hasActiveHand())
				return;
			actions.add(Action.TAKE);
			actions.add(Action.STAND);
			if (!getFirstHand().isActive()) {
				return;
			}
			if (getFirstHand().getCards().size() != 2)
				return;
			Card first = getFirstHand().getCards().get(0);
			Card second = getFirstHand().getCards().get(1);
			if (first.getRank() == second.getRank() && !hasSecondHand())
				actions.add(Action.SPLIT);
			else if (first.getRank() > 9 && second.getRank() > 9 && !hasSecondHand())
				actions.add(Action.SPLIT);
			int handValue = getFirstHand().calculateValues().get(0);
			if (handValue >= 9 && handValue <= 11 && !hasSecondHand())
				actions.add(Action.DOUBLE);
		} finally {
			releasePlayerLock();
		}
	}

	private IHand getSecondHand() {
		return getHands().get(1);
	}

	private boolean hasSecondHand() {
		return getHands().size() > 1;
	}

	public void prepareNextRound() {
		try {
			tryTakingPlayerLock();
			hands = new ArrayList<IHand>();
			hands.add(createNewHand(true));
			updateActions();
			this.removeTotalBet();
		} finally {
			releasePlayerLock();
		}
	}

	public boolean hasActiveHand() {
		return hands.stream().filter(hand -> hand.isActive()).findAny().isPresent();
	}

	public boolean isInsuranceCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	public void splitStartingHand() {
		try {
			tryTakingPlayerLock();
			validateSplitPreConditions();
			BetUtil.verifySufficentBalance(hands.get(0).getBet(), this); // Check balance after getting lock
			IHand splitHand = new BlackjackHand(UUID.randomUUID(), false);
			Card cardFromStartingHand = hands.get(0).getCards().remove(1);
			splitHand.addCard(cardFromStartingHand);
			splitHand.updateBet(hands.get(0).getBet());// immutable BigDecimal
			updateBalanceAndBet(splitHand.getBet());
			hands.add(splitHand);
		} finally {
			releasePlayerLock();
		}
	}

	public void stand() {
		try {
			tryTakingPlayerLock();
			IHand activeHand = getActiveHand();
			activeHand.stand();
			if (shouldActivateSecondHand(activeHand))
				activateSecondHand();
		} finally {
			releasePlayerLock();
		}
	}

	private boolean shouldActivateSecondHand(IHand activeHand) {
		return getHands().indexOf(activeHand) == 0 && getHands().size() == 2;
	}

	private void activateSecondHand() {
		getSecondHand().activate();
	}

	public void doubleDown(Card ref) {
		try {
			tryTakingPlayerLock();
			validateDoubleDownPreConditions();
			updateBalanceAndBet(getTotalBet());
			getFirstHand().doubleDown(ref);
		} finally {
			releasePlayerLock();
		}
	}

	@Override
	public void updateStartingBet(BigDecimal bet, ICasinoTable table) {
		try {
			tryTakingPlayerLock();
			super.updateStartingBet(bet, table);
			// getFirstHand().updateBet(bet);
		} finally {
			releasePlayerLock();
		}
	}

	public void insure() {
		try {
			tryTakingPlayerLock();
			validateInsuringConditions();
			getFirstHand().insure();
			updateBalanceAndBet(getFirstHand().getBet().divide(BigDecimal.TWO));
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}
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
			throw new IllegalPlayerActionException("blackjack cannot be doubled ", 10);
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
		try {
			tryTakingPlayerLock();
			hand.addCard(card);
		} finally {
			releasePlayerLock();
		}
	}

	public void clearHands() {
		tryTakingPlayerLock();
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
		try {
			tryTakingPlayerLock();
			super.reset();
			hands = new ArrayList<IHand>();
		} finally {
			releasePlayerLock();
		}
		// hands.add(createNewHand(true));
	}

	@Override
	public boolean isCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	private IHand getFirstHand() {
		return getHands().get(0);
	}

	public boolean hasDoubled() {
		return getFirstHand().isDoubled();
	}

	public boolean hasInsured() {
		return getFirstHand().isInsured();
	}

	public boolean hasSplit() {
		return hands.size() == 2;
	}

	@Override
	public BigDecimal getInsuranceAmount() {
		return getFirstHand().getInsuranceBet();
	}

	public boolean hasCompletedFirstHand() {
		return getFirstHand().isCompleted();
	}

	public Integer getFirstHandFinalValue() {
		IHand hand = getFirstHand();
		return hand.isCompleted() ? hand.getFinalValue() : null;
	}

	public BigDecimal getBet(int handNumber) {
		if (handNumber < 0 || handNumber > 1)
			throw new IllegalArgumentException("no such hand " + handNumber);
		return getHands().get(handNumber).getBet();
	}

	// first or second hand can be active. second hand might need additional card to
	// reach minimum of 2 cards
	@Override
	public <T> T autoplay(T t) {
		if (!hasActiveHand() || !(t instanceof Card card)) {
			return t;
		}
		if (getFirstHand().isActive())
			getFirstHand().complete();
		if (getHands().size() != 2)
			return t;
		IHand secondHand = getSecondHand();
		if (secondHand.getCards().size() < 2)
			secondHand.addCard(card);
		if (!secondHand.isCompleted())// Automatic blackjack can have completed the hand
			secondHand.complete();
		return null;
	}

}
