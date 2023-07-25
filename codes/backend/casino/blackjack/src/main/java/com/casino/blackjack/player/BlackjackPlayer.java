package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.casino.blackjack.export.BlackjackPlayerAction;
import com.casino.common.bet.BetVerifier;
import com.casino.common.cards.Card;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.structure.ISeatedTable;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIncludeProperties(value = { "hands", "actions", "seatNumber", "userName", "currentBalance", "totalBet", "payout" })
public final class BlackjackPlayer extends CasinoPlayer {
	private List<BlackjackHand> hands;
	private List<BlackjackPlayerAction> actions;
	private Integer seatNumber;

	public BlackjackPlayer(User user, ISeatedTable<BlackjackPlayer> table) {
		super(user, table);
		hands = new ArrayList<>();
		hands.add(createNewHand());
		actions = new ArrayList<>(4);
	}

	public boolean canTake() {
		BlackjackHand hand = getActiveHand();
		if (hand == null)
			return false;
		List<Integer> values = hand.calculateValues(); // Smallest value in 0 pos.
		return values.get(0) < 21;
	}

	private BlackjackHand createNewHand() {
		return new BlackjackHand(UUID.randomUUID(), true);
	}

	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}

	// UI checks player insurance capability. Backend validates still.
	public void updateAvailableActions() {
		try {
			tryLock();
			if (!hasActiveHand())
				return;
			actions = new ArrayList<>();
			actions.add(BlackjackPlayerAction.TAKE);
			actions.add(BlackjackPlayerAction.STAND);
			if (BlackjackActionValidator.isDoubleDownTechnicallyAllowed(this))
				actions.add(BlackjackPlayerAction.DOUBLE_DOWN);
			if (BlackjackActionValidator.isSplitTechnicallyAllowed(this))
				actions.add(BlackjackPlayerAction.SPLIT);
		} finally {
			releaseLock();
		}
	}

	private BlackjackHand getSecondHand() {
		return getHands().get(1);
	}

	@Override
	public void prepareForNextRound() {
		try {
			tryLock();
			hands = new ArrayList<>();
			hands.add(createNewHand());
			updateAvailableActions();
			this.removeTotalBet();
		} finally {
			releaseLock();
		}
	}

	@Override
	public boolean hasActiveHand() {
		return hands.stream().anyMatch(BlackjackHand::isActive);
	}

	@Override
	public List<BlackjackPlayerAction> getActions() {
		return actions;
	}

	public void splitStartingHand() {
		try {
			tryLock();
			BlackjackActionValidator.validateSplitAction(this);
			BlackjackHand splitHand = new BlackjackHand(UUID.randomUUID(), false);
			Card cardFromStartingHand = hands.get(0).getCards().remove(1);
			splitHand.addCard(cardFromStartingHand);
			splitHand.updateBet(hands.get(0).getBet());
			updateBalanceAndTotalBet(splitHand.getBet());
			hands.add(splitHand);
		} finally {
			releaseLock();
		}
	}

	public void stand() {
		try {
			tryLock();
			BlackjackHand activeHand = getActiveHand();
			activeHand.stand();
		} finally {
			releaseLock();
		}
	}

	public void activateSecondHand() {
		try {
			tryLock();
			getSecondHand().activate();
		} finally {
			releaseLock();
		}
	}

	public void doubleDown(Card ref) {
		try {
			tryLock();
			BlackjackActionValidator.validateDoubleDownAction(this);
			updateBalanceAndTotalBet(getTotalBet());
			getFirstHand().doubleDown(ref);
		} finally {
			releaseLock();
		}
	}

	public void updateStartingBet(BigDecimal bet) {
		try {
			tryLock();
			BetVerifier.verifyBetIsAllowedInTable(table, this, bet);
			this.totalBet = bet;
		} finally {
			releaseLock();
		}
	}
	@JsonProperty // getter for serialization
	public Integer getSeatNumber() {
		return seatNumber;
	}

	public void insure() {
		try {
			tryLock();
			BlackjackActionValidator.validateInsureAction(this);
			getFirstHand().insure();
			updateBalanceAndTotalBet(getFirstHand().getBet().divide(BigDecimal.TWO,RoundingMode.DOWN));
		} finally {
			releaseLock();
		}
	}

	public BlackjackHand getActiveHand() {
		return hands.stream().filter(BlackjackHand::isActive).findFirst().orElse(null); // to Optional
	}

	public void hit(Card card) {
		try {
			tryLock();
			getActiveHand().addCard(card);
		} finally {
			releaseLock();
		}
	}

	public List<BlackjackHand> getHands() {
		return hands;
	}

	@Override
	public String toString() {
		return "BlackjackPlayer [hands=" + hands + ", actions=" + actions + ", seatNumber=" + seatNumber + ", toString()=" + super.toString() + "]";
	}

	@Override
	public boolean canAct() {
		return getStatus() == PlayerStatus.ACTIVE && getActiveHand() != null;
	}

	public boolean hasWinningChance() {
		return hands.stream().anyMatch(BlackjackHand::hasWinningChance);
	}

	@Override
	public void reset() {
		try {
			tryLock();
			super.reset();
			hands = new ArrayList<>();
		} finally {
			releaseLock();
		}
	}

	public BlackjackHand getFirstHand() {
		return getHands().get(0);
	}

	public boolean hasDoubled() {
		return getFirstHand().isDoubled();
	}

	public boolean hasInsured() {
		return getFirstHand().isInsured();
	}

	public boolean hasCompletedFirstHand() {
		return getFirstHand().isCompleted();
	}

	public Integer getFirstHandFinalValue() {
		BlackjackHand hand = getFirstHand();
		return hand.isCompleted() ? hand.calculateFinalValue() : null;
	}

	public BigDecimal getBet(int handNumber) {
		if (handNumber < 0 || handNumber > 1)
			throw new IllegalArgumentException("no such hand " + handNumber);
		return getHands().get(handNumber).getBet();
	}

	public <T> Optional<T> autoplay(T t) {
		Optional<T> cardOptional = Optional.of(t);
		if (!hasActiveHand() || !(t instanceof Card card))
			return cardOptional;
		if (getFirstHand().isActive())
			getFirstHand().complete();
		if (getHands().size() != 2)
			return cardOptional;
		BlackjackHand secondHand = getSecondHand();
		if (secondHand.getCards().size() < 2) {
			secondHand.addCard(card); // card is used here
			cardOptional = Optional.empty();
		}
		if (!secondHand.isCompleted())// blackjack can have completed the hand automatically
			secondHand.complete();
		return cardOptional;
	}

}
