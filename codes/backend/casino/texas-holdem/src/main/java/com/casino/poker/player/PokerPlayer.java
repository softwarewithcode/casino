package com.casino.poker.player;

import com.casino.common.cards.Card;
import com.casino.common.player.ICasinoPlayer;
import com.casino.poker.actions.PokerAction;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.bet.BetToken;
import com.casino.poker.hand.PokerHand;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface PokerPlayer extends ICasinoPlayer {

    void createPokerHand(List<Card> tableCards);

    PokerHand getHand();

    BigDecimal getTableChipCount(); //TODO count

    boolean hasChipsOnTable();

    void addChipsOnTable(BigDecimal increaseAmount);

    void takeChipsBackFromTable();

    void removeSomeChipsFromTable(BigDecimal removeAmount);

    void call(BigDecimal callAmount);

    void betOrRaise(BigDecimal raiseAmount);

    void fold();

    void check();

    void allIn();

    List<Card> getHoleCards();

    void addHoleCard(Card card);

    boolean hasActionType(PokerActionType type);

    Set<BetToken> getMissingBlindBetTokens();

    void addMissingBlindBetToken(BetToken token);

    void clearMissedBlindBets();

    void sitOut(boolean immediate);

    void continueGame(boolean immediate);

    void setWaitBigBlind(boolean waiBigBlind);

    void setSeatNumber(Integer seatNumber);

    Integer getSeatNumber();

    List<PokerAction> getActions();

    boolean hasHoleCards();

    boolean isNew();

    boolean isActive();

    boolean isWaitingBigBlind();

    boolean shouldAutoPlay();

    boolean isAllIn();

    void clearActed();

    void subtractFromBalance(BigDecimal amount);

    Boolean hasMostChipsOnTable(BigDecimal biggestAmountMoneyOnTable);

    boolean hasBalance();
}
