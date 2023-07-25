package com.casino.poker.player;

import com.casino.common.bet.BetVerifier;
import com.casino.common.cards.Card;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.functions.Functions;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.reload.Reloadable;
import com.casino.common.table.structure.ISeatedTable;
import com.casino.common.user.User;
import com.casino.poker.actions.PokerAction;
import com.casino.poker.actions.PokerActionCreator;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.bet.BetToken;
import com.casino.poker.dealer.HoldemDealer;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.hand.HandFactory;
import com.casino.poker.hand.HoldemHand;
import com.casino.poker.hand.PokerHand;
import com.casino.poker.table.HoldemTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(value = {"holeCards"}) // Explicitly ignore holeCards.
@JsonIncludeProperties(value = {"hand", "chipsOnTable", "seatNumber", "actions", "userName", "currentBalance", "chipsOnTable", "cardsBackSideVisible", "status", "cards"})
public final class HoldemPlayer extends CasinoPlayer implements PokerPlayer, Reloadable {
    private final List<Card> holeCards;
    private final Set<BetToken> missingBlindBets;
    private HoldemHand hand;
    private BigDecimal chipsOnTable;
    private Integer seatNumber;
    private List<PokerAction> actions;
    private boolean sitsOutNextHand;
    private boolean waitBigBlind;
    private boolean hasActed;

    public HoldemPlayer(User user, ISeatedTable<PokerPlayer> table) {
        super(user, table);
        chipsOnTable = BigDecimal.ZERO;
        this.holeCards = new ArrayList<>(2);
        this.missingBlindBets = new HashSet<>();
    }

    @Override
    public boolean isAllIn() {
        return hasHoleCards() && !hasBalance();
    }

    @Override
    public boolean hasHoleCards() {
        return holeCards != null && holeCards.size() > 0;
    }

    public List<Card> getCards() {// For serialization
        if (((HoldemDealer) table.getDealer()).isShowdown())
            return holeCards;
        if (holeCards.size() >= 2)
            return Collections.emptyList();
        return null;
    }

    @Override
    public boolean canAct() {
        return !isAllIn() && !hasActed && hasActiveHand();
    }

    @Override
    public boolean isWaitingBigBlind() {
        return waitBigBlind;
    }

    @Override
    public void setWaitBigBlind(boolean waitBigBlind) {
        this.waitBigBlind = waitBigBlind;
    }

    @Override
    public boolean hasActiveHand() {
        return hasHoleCards();
    }

    @Override
    public void prepareForNextRound() {
        try {
            tryLock();
            this.hasActed = false;
            if (this.holeCards != null)
                this.holeCards.clear();
            if (this.actions != null)
                this.actions.clear();
            this.hand = null;
            if (this.sitsOutNextHand && getStatus() == PlayerStatus.ACTIVE)
                setStatus(PlayerStatus.SIT_OUT);
            else if (this.sitsOutNextHand && getStatus() == PlayerStatus.NEW)
                setStatus(PlayerStatus.SIT_OUT_AS_NEW);
            else if (!this.sitsOutNextHand && getStatus() == PlayerStatus.SIT_OUT_AS_NEW)
                setStatus(PlayerStatus.NEW);
            else if (!this.sitsOutNextHand && getStatus() == PlayerStatus.SIT_OUT)
                setStatus(PlayerStatus.ACTIVE);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void updateAvailableActions() {
        try {
            tryLock();
            this.actions = new PokerActionCreator().createActions(this);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void clearAvailableActions() {
        super.clearAvailableActions();
    }

    @Override
    public boolean shouldAutoPlay() {
        PlayerStatus status = getStatus();
        return status == PlayerStatus.SIT_OUT || status == PlayerStatus.LEFT && hasHoleCards() && !isAllIn();
    }

    @JsonIgnore
    @Override
    public HoldemTable getTable() {
        return (HoldemTable) super.getTable();
    }

    @Override
    public Boolean hasMostChipsOnTable(BigDecimal biggestAmountMoneyOnTable) {
        HoldemTable table = getTable();
        int playerCountWithMostMoneyOnTable = table.getRound().getPlayers().stream().filter(player -> Functions.isFirstMoreOrEqualToSecond.apply(getTableChipCount(), biggestAmountMoneyOnTable)).toList().size();
        if (playerCountWithMostMoneyOnTable != 1)
            return false;
        return Functions.isFirstMoreOrEqualToSecond.apply(getTableChipCount(), biggestAmountMoneyOnTable);
    }

    @Override
    public boolean hasBalance() {
        return super.hasBalance();
    }

    @Override
    public List<PokerAction> getActions() {
        if (actions == null)
            this.actions = new ArrayList<>();
        return actions;
    }

    @Override
    public Integer getSeatNumber() {
        return seatNumber;
    }

    @Override
    public boolean hasChipsOnTable() {
        return chipsOnTable != null && Functions.isFirstMoreThanSecond.apply(chipsOnTable, BigDecimal.ZERO);
    }

    @Override
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public void addChipsOnTable(BigDecimal increaseAmount) {
        try {
            tryLock();
            verifyCoversAmount(increaseAmount);
            if (chipsOnTable == null)
                chipsOnTable = BigDecimal.ZERO;
            chipsOnTable = chipsOnTable.add(increaseAmount);
            updateBalanceAndTotalBet(increaseAmount);
        } finally {
            releaseLock();
        }
    }

    private void verifyCoversAmount(BigDecimal requiredAmount) {
        if (!coversAmount(requiredAmount))
            throw new IllegalBetException("Amount:" + requiredAmount + " is more than player can afford:" + getCurrentBalance(), 100);
    }

    @Override
    public void takeChipsBackFromTable() {
        try {
            tryLock();
            increaseBalance(chipsOnTable);
            chipsOnTable = BigDecimal.ZERO;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void removeSomeChipsFromTable(BigDecimal removeAmount) {
        try {
            tryLock();
            if (!Functions.isFirstMoreOrEqualToSecond.apply(chipsOnTable, removeAmount))
                throw new IllegalArgumentException("Cannot remove " + removeAmount + " from table:" + chipsOnTable);
            chipsOnTable = chipsOnTable.subtract(removeAmount);
        } finally {
            releaseLock();
        }
    }

    @Override
    public BigDecimal getTableChipCount() {
        return chipsOnTable != null ? chipsOnTable.setScale(2, RoundingMode.DOWN) : BigDecimal.ZERO;
    }

    @Override
    public PokerHand getHand() {
        return this.hand;
    }

    @Override
    public String toString() {
        return "userName:" + getUserName() + " seat:" + seatNumber + " moneyOnTable:" + chipsOnTable + " hand:" + getHand() + " balance:" + getCurrentBalance() + " waitingBB:" + isWaitingBigBlind() + " hasActed:" + hasActed
                + " hasHoleCards:" + hasHoleCards() + " isAllIn:" + isAllIn() + " id:" + getId() + " status:" + getStatus();
    }

    @Override
    public void createPokerHand(List<Card> tableCards) {
        try {
            if (tableCards.size() != 5)
                throw new IllegalStateException("Should be 5 tableCards while assigning pokerHand. Was:" + tableCards.size());
            tryLock();
            this.hand = HandFactory.constructPokerHand(tableCards, getHoleCards());
        } finally {
            releaseLock();
        }
    }

    @Override
    public void call(BigDecimal missingFromTable) {
        try {
            tryLock();
            BetVerifier.verifySufficientBalance(missingFromTable, this);
            chipsOnTable = chipsOnTable.add(missingFromTable);
            updateBalanceAndTotalBet(missingFromTable);
            hasActed = true;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void betOrRaise(BigDecimal raiseAmount) {
        try {
            tryLock();
            BigDecimal additionalAmount = raiseAmount.subtract(getTableChipCount());
            BetVerifier.verifySufficientBalance(additionalAmount, this);
            chipsOnTable = chipsOnTable.add(additionalAmount);
            updateBalanceAndTotalBet(additionalAmount);
            hasActed = true;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void fold() {
        try {
            tryLock();
            hasActed = true;
            this.hand = null;
            this.holeCards.clear();
        } finally {
            releaseLock();
        }
    }

    @Override
    public void check() {
        try {
            tryLock();
            hasActed = true;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void allIn() {
        try {
            tryLock();
            hasActed = true;
            chipsOnTable = chipsOnTable.add(getCurrentBalance());
            updateBalanceAndTotalBet(getCurrentBalance());
        } finally {
            releaseLock();
        }
    }

    /**
     * Returns the amount that was consumed from attempt ( added to balance)
     */
    @Override
    public BigDecimal tryFillUpToLimit(BigDecimal additionalAmount, BigDecimal fillUpLimit) {
        try {
            tryLock();
            BigDecimal maxAllowedAddition = fillUpLimit.subtract(getCurrentBalance());
            if (Functions.isFirstMoreOrEqualToSecond_(BigDecimal.ZERO, maxAllowedAddition))
                return BigDecimal.ZERO;
            BigDecimal balanceIncrease = Functions.calculateIncreaseAmount(additionalAmount, maxAllowedAddition);
            increaseBalance(balanceIncrease);
            return balanceIncrease;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void clearActed() {
        try {
            tryLock();
            this.hasActed = false;
        } finally {
            releaseLock();
        }
    }

    @JsonIgnoreProperties // Explicitly ignore holeCards from serialization
    @Override
    public List<Card> getHoleCards() {
        return holeCards;
    }

    @Override
    public void addHoleCard(Card card) {
        try {
            tryLock();
            if (holeCards.size() > 2)
                throw new IllegalArgumentException("Too many holeCards " + getUserName());
            holeCards.add(card);
        } finally {
            releaseLock();
        }
    }

    @Override
    public boolean hasActionType(PokerActionType type) {
        return HoldemFunctions.hasAction.apply(this, type);
    }

    @Override
    public Set<BetToken> getMissingBlindBetTokens() {
        return missingBlindBets.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void addMissingBlindBetToken(BetToken token) {
        try {
            if (token == null)
                throw new IllegalArgumentException("Token is required");
            takeLock();
            missingBlindBets.add(token);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void clearMissedBlindBets() {
        try {
            takeLock();
            missingBlindBets.clear();
        } finally {
            releaseLock();
        }
    }

    @Override
    public void sitOut(boolean immediate) {
        try {
            takeLock();
            if (immediate) {
                var nextStatus = getStatus() == PlayerStatus.NEW ? PlayerStatus.SIT_OUT_AS_NEW : PlayerStatus.SIT_OUT;
                setStatus(nextStatus);
            }
            this.sitsOutNextHand = true;
        } finally {
            releaseLock();
        }
    }

    @Override
    public void continueGame(boolean immediate) {
        try {
            takeLock();
            if (immediate)
                setStatus(PlayerStatus.ACTIVE);
            this.sitsOutNextHand = false;
        } finally {
            releaseLock();
        }
    }

    public BigDecimal getChipsOnTable() {
        return chipsOnTable; // For serialization
    }
}
