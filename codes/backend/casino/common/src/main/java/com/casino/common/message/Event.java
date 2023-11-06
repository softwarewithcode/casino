package com.casino.common.message;

public enum Event implements MessageTitle {
    NEW_PLAYER,
    BET_TIME_START,
    INSURANCE_TIME_START,
    PLAYER_TIME_START,
    INITIAL_DEAL_DONE,
    NO_BETS_NO_DEAL,
    LOGIN,
    ROUND_COMPLETED,
    PLAYER_LEFT,
    STATUS_UPDATE,
    OPEN_TABLE,
    TIMED_OUT,
    SIT_OUT,
    SHOWDOWN,
    SPINNING,
    INIT_DATA
}
