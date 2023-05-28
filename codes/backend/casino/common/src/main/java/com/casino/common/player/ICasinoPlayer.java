package com.casino.common.player;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.common.functions.Functions;
import com.casino.common.table.structure.ICasinoTable;
import com.casino.common.action.PlayerAction;
import com.casino.common.table.timing.TimeControl;

public interface ICasinoPlayer {
    String getUserName();

    void increaseBalance(BigDecimal amount);

    void increaseBalanceAndPayout(BigDecimal amount);

    UUID getId();

    boolean canAct();

    PlayerStatus getStatus();

    void setStatus(PlayerStatus status);

    BigDecimal getTotalBet();

    boolean hasBet();

    void reset();

    void subtractTotalBetFromBalance();

    BigDecimal getCurrentBalance();

    boolean hasActiveHand();

    void removeTotalBet();

    void prepareForNextRound();

    <T> void sendMessage(T message);

    void updateAvailableActions();

    default void clearAvailableActions() {
        getActions().clear();
    }

    List<? extends PlayerAction> getActions();

    void increaseSkips();

    void clearSkips();

    boolean shouldStandUp();

    boolean isConnected();

    TimeControl getTimeControl();

    boolean isActive();

    default boolean isSitOut() {
        return getStatus() == PlayerStatus.SIT_OUT;
    }

    ICasinoTable getTable();

    default boolean coversAmount(BigDecimal requiredAmount) {
        return Functions.isFirstMoreOrEqualToSecond.apply(getCurrentBalance(), requiredAmount);
    }

}
