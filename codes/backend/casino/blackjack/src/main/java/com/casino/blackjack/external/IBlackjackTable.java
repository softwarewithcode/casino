package com.casino.blackjack.external;

import java.math.BigDecimal;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.player.ICasinoPlayer;

/*
 * Describes possible actions for blackjack tables
 */
public interface IBlackjackTable {
	public boolean trySeat(int seatNumber, ICasinoPlayer player);

	public void placeStartingBet(ICasinoPlayer player, BigDecimal bet);

	// public void splitStartingHand(String playerId);
	public void splitStartingHand(BlackjackPlayer player);

	public void doubleStartingBet(ICasinoPlayer player);

	public void takeCard(ICasinoPlayer player);

	public void stand(BlackjackPlayer player); // no more cards

	public void insure(ICasinoPlayer player);

}
