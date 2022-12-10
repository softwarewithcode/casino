package com.casino.blackjack.external;

import java.math.BigDecimal;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.common.player.ICasinoPlayer;

public interface IBlackjackTable {
	public boolean join(int seatNumber, ICasinoPlayer player);

	public void bet(ICasinoPlayer player, BigDecimal bet);

	// public void splitStartingHand(String playerId);
	public void split(BlackjackPlayer player);

	public void doubleDown(BlackjackPlayer player);

	public void hit(BlackjackPlayer player);

	public void stand(BlackjackPlayer player); // no more cards

	public void insure(BlackjackPlayer player);

	public void onInsurancePhaseEnd();

}
