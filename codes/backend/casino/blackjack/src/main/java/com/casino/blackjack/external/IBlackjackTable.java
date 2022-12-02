package com.casino.blackjack.external;

import java.math.BigDecimal;

import com.casino.common.player.ICasinoPlayer;

/*
 * Describes available actions for blackjack tables
 */
public interface IBlackjackTable {
	public boolean trySeat(int seatNumber, ICasinoPlayer player);

	public void placeStartingBet(ICasinoPlayer player, BigDecimal bet);

	public void splitStartingHand(ICasinoPlayer player);

	public void doubleStartingBet(ICasinoPlayer player);

	public void takeCard(ICasinoPlayer player);

	public void stand(ICasinoPlayer player);

}
