package com.casino.common.player;

import com.casino.common.action.PlayerAction;
import com.casino.common.functions.Functions;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.table.timing.TimeControl;
import com.casino.common.user.Connectable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CasinoPlayer extends Connectable {
    String getUserName();

    void increaseBalance(BigDecimal amount);

    void increaseBalanceAndPayout(BigDecimal amount); // payout=winAmount?
    
    boolean hasTooManySkips();

    UUID getId();


    PlayerStatus getStatus();

    void setStatus(PlayerStatus status);

    BigDecimal getTotalBet();

    boolean hasBet();

    void reset();

    void subtractTotalBetFromBalance();

    BigDecimal getCurrentBalance();

    void removeTotalBet();

    void prepareForNextRound();

    void updateAvailableActions();

    default void clearAvailableActions() {
        getActions().clear();
    }

    List<? extends PlayerAction> getActions();

    void increaseSkips();

    void clearSkips();

    boolean shouldStandUp();

    TimeControl getTimeControl();

    boolean isActive();

    default boolean isSitOut() {
        return getStatus() == PlayerStatus.SIT_OUT;
    }

    CasinoTable getTable();

    default boolean coversAmount(BigDecimal requiredAmount) {
        return Functions.isFirstMoreOrEqualToSecond.apply(getCurrentBalance(), requiredAmount);
    }

}
