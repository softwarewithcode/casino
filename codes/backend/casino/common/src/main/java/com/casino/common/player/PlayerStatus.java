package com.casino.common.player;

public enum PlayerStatus {
    SIT_OUT_NEXT_HAND(false, true),
    ACTIVE(false, false),
    LEFT(false, false),
    NEW(true, false),
    SIT_OUT(false, true),
    SIT_OUT_AS_NEW(true, true);

    private boolean newStatus;
    private boolean sitoutStatus;

    private PlayerStatus(boolean isNew, boolean isSittingOut) {
        this.newStatus = isNew;
        this.sitoutStatus = isSittingOut;
    }

    public boolean isNewStatus() {
        return newStatus;
    }

    public boolean isSitoutStatus() {
        return sitoutStatus;
    }
}