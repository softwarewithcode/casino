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
import com.casino.common.player.Player;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.structure.ISeatedTable;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIncludeProperties(value = { "hands", "actions", "seatNumber", "userName", "currentBalance", "totalBet", "payout" })
public final class BlackjackPlayer_ extends Player implements BlackjackPlayer {
	private List<BlackjackHand> hands;
	private List<BlackjackPlayerAction> actions;
	private Integer seatNumber;

	public BlackjackPlayer_(User user, ISeatedTable<BlackjackPlayer_> table) {
		super(user, table);
		hands = new ArrayList<>();
		hands.add(createNewHand());
		actions = new ArrayList<>(4);
	}

	@Override
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
	@Override
	public void updateAvailableActions() {
		try {
			tryLockOrThrow();
			actions = new ArrayList<>();
			actions.add(BlackjackPlayerAction.REFRESH);
			if (!hasActiveHand())
				return;
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
			tryLockOrThrow();
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

	@Override
	public void splitStartingHand() {
		try {
			tryLockOrThrow();
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

	@Override
	public void stand() {
		try {
			tryLockOrThrow();
			BlackjackHand activeHand = getActiveHand();
			activeHand.stand();
		} finally {
			releaseLock();
		}
	}

	@Override
	public void activateSecondHand() {
		try {
			tryLockOrThrow();
			getSecondHand().activate();
		} finally {
			releaseLock();
		}
	}

	@Override
	public void doubleDown(Card ref) {
		try {
			tryLockOrThrow();
			BlackjackActionValidator.validateDoubleDownAction(this);
			updateBalanceAndTotalBet(getTotalBet());
			getFirstHand().doubleDown(ref);
		} finally {
			releaseLock();
		}
	}

	@Override
	public void updateStartingBet(BigDecimal bet) {
		try {
			tryLockOrThrow();
			BetVerifier.verifyBetIsAllowedInTable(table, this, bet);
			super.updateTotalBet(bet);
		} finally {
			releaseLock();
		}
	}

	@JsonProperty // getter for serialization
	public Integer getSeatNumber() {
		return seatNumber;
	}

	@Override
	public void insure() {
		try {
			tryLockOrThrow();
			BlackjackActionValidator.validateInsureAction(this);
			getFirstHand().insure();
			updateBalanceAndTotalBet(getFirstHand().getBet().divide(BigDecimal.TWO, RoundingMode.DOWN));
		} finally {
			releaseLock();
		}
	}

	@Override
	public BlackjackHand getActiveHand() {
		return hands.stream().filter(BlackjackHand::isActive).findFirst().orElse(null); // to Optional
	}

	@Override
	public void hit(Card card) {
		try {
			tryLockOrThrow();
			getActiveHand().addCard(card);
		} finally {
			releaseLock();
		}
	}

	@Override
	public List<BlackjackHand> getHands() {
		return hands;
	}

	@Override
	public String toString() {
		return "BlackjackPlayer [hands=" + hands + ", actions=" + actions + ", seatNumber=" + seatNumber
				+ ", toString()=" + super.toString() + "]";
	}

	@Override
	public boolean canAct() {
		return getStatus() == PlayerStatus.ACTIVE && getActiveHand() != null;
	}

	@Override
	public void reset() {
		try {
			tryLockOrThrow();
			super.reset();
			hands = new ArrayList<>();
		} finally {
			releaseLock();
		}
	}

	@Override
	public BlackjackHand getFirstHand() {
		return getHands().get(0);
	}

	@Override
	public boolean hasDoubled() {
		return getFirstHand().isDoubled();
	}

	@Override
	public boolean hasInsured() {
		return getFirstHand().isInsured();
	}

	@Override
	public boolean hasCompletedFirstHand() {
		return getFirstHand().isCompleted();
	}

	@Override
	public Integer getFirstHandFinalValue() {
		BlackjackHand hand = getFirstHand();
		return hand.isCompleted() ? hand.calculateFinalValue() : null;
	}

	@Override
	public BigDecimal getBet(int handNumber) {
		if (handNumber < 0 || handNumber > 1)
			throw new IllegalArgumentException("no such hand " + handNumber);
		return getHands().get(handNumber).getBet();
	}
	public boolean hasWinningChance() {
		return hands.stream().anyMatch(BlackjackHand::hasWinningChance);
	}
	@Override  //Could use directly Card
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
