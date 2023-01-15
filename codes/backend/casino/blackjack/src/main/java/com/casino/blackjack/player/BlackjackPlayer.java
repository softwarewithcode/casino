package com.casino.blackjack.player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetVerifier;
import com.casino.common.cards.Card;
import com.casino.common.cards.IHand;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.ICasinoTable;
import com.casino.common.table.ISeatedTable;
import com.casino.common.user.Bridge;
import com.casino.common.user.PlayerAction;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = { "hands", "actions", "seatNumber", "userName", "balance", "totalBet", "payout" })
public class BlackjackPlayer extends CasinoPlayer {
	private List<IHand> hands;
	private List<PlayerAction> actions;
	private Integer seatNumber;

	public BlackjackPlayer(Bridge bridge, ISeatedTable table) {
		super(bridge, table);
		hands = new ArrayList<>();
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

	public Integer getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}

	public void updateAvailableActions() {
		try {
			tryTakingPlayerLock();
			if (!hasActiveHand())
				return;
			actions = new ArrayList<>();
			actions.add(PlayerAction.TAKE);
			actions.add(PlayerAction.STAND);
			if (!getFirstHand().isActive())
				return;
			if (getFirstHand().getCards().size() != 2)
				return;
			if (isSplitAllowed())
				actions.add(PlayerAction.SPLIT);
			if (isDoubleDownAllowed())
				actions.add(PlayerAction.DOUBLE_DOWN);
		} finally {
			releasePlayerLock();
		}
	}

	private boolean isDoubleDownAllowed() {
		int handValue = getFirstHand().calculateValues().get(0);
		return handValue >= 9 && handValue <= 11 && !hasSecondHand();
	}

	private boolean isSplitAllowed() {
		Card first = getFirstHand().getCards().get(0);
		Card second = getFirstHand().getCards().get(1);
		return BlackjackUtil.haveSameValue(first, second) && !hasSecondHand() && !getFirstHand().isInsured();
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
			hands = new ArrayList<>();
			hands.add(createNewHand(true));
			updateAvailableActions();
			this.removeTotalBet();
		} finally {
			releasePlayerLock();
		}
	}

	public boolean hasActiveHand() {
		return hands.stream().filter(IHand::isActive).findAny().isPresent();
	}

	public boolean isInsuranceCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	public List<PlayerAction> getActions() {
		return actions;
	}

	public void splitStartingHand() {
		try {
			tryTakingPlayerLock();
			ActionValidator.validateSplitPreConditions(this);
			IHand splitHand = new BlackjackHand(UUID.randomUUID(), false);
			Card cardFromStartingHand = hands.get(0).getCards().remove(1);
			splitHand.addCard(cardFromStartingHand);
			splitHand.updateBet(hands.get(0).getBet());
			updateBalanceAndTotalBet(splitHand.getBet());
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
		} finally {
			releasePlayerLock();
		}
	}

	public void activateSecondHand() {
		getSecondHand().activate();
	}

	public void doubleDown(Card ref) {
		try {
			tryTakingPlayerLock();
			ActionValidator.validateDoubleDownPreConditions(this);
			updateBalanceAndTotalBet(getTotalBet());
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
		} finally {
			releasePlayerLock();
		}
	}

	public void insure() {
		try {
			tryTakingPlayerLock();
			ActionValidator.validateInsuringConditions(this);
			getFirstHand().insure();
			updateBalanceAndTotalBet(getFirstHand().getBet().divide(new BigDecimal("2")));
		} finally {
			releasePlayerLock();
		}
	}

	public IHand getActiveHand() {
		return hands.stream().filter(IHand::isActive).findFirst().orElse(null); //to Optional
	}

	public void hit(Card card) {
		try {
			tryTakingPlayerLock();
			getActiveHand().addCard(card);
		} finally {
			releasePlayerLock();
		}
	}

	@Override
	public List<IHand> getHands() {
		return hands;
	}

	@Override
	public String toString() {
		return "BlackjackPlayer [hands=" + hands + ", actions=" + actions + ", seatNumber=" + seatNumber + ", toString()=" + super.toString() + "]";
	}

	@Override
	public boolean canAct() {
		return getActiveHand() != null;
	}

	@Override
	public boolean hasWinningChance() {
		return hands.stream().filter(IHand::hasWinningChance).findAny().isPresent();
	}

	@Override
	public void reset() {
		try {
			tryTakingPlayerLock();
			super.reset();
			hands = new ArrayList<>();
		} finally {
			releasePlayerLock();
		}
	}

	@Override
	public boolean isCompensable() {
		return getFirstHand().isInsuranceCompensable();
	}

	public IHand getFirstHand() {
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
		return hand.isCompleted() ? hand.calculateFinalValue() : null;
	}

	public BigDecimal getBet(int handNumber) {
		if (handNumber < 0 || handNumber > 1)
			throw new IllegalArgumentException("no such hand " + handNumber);
		return getHands().get(handNumber).getBet();
	}

	@Override
	public <T> Optional<T> autoplay(T t) {
		Optional<T> cardOptional = Optional.of(t);
		if (!hasActiveHand() || !(t instanceof Card card))
			return cardOptional;
		if (getFirstHand().isActive())
			getFirstHand().complete();
		if (getHands().size() != 2)
			return cardOptional;
		IHand secondHand = getSecondHand();
		if (secondHand.getCards().size() < 2) {
			secondHand.addCard(card); // card is used here
			cardOptional = Optional.empty();
		}
		if (!secondHand.isCompleted())// blackjack can have completed the hand automatically
			secondHand.complete();
		return cardOptional;
	}

}
