package com.casino.poker.actions;

import com.casino.common.action.PlayerAction;
import com.casino.poker.player.HoldemPlayer;

import java.util.List;

public interface ActionCreator {

 List<? extends PlayerAction> createActions(HoldemPlayer player);

 //<T extends ICasinoPlayer> List<? extends PlayerAction> createActions(T player);
}
