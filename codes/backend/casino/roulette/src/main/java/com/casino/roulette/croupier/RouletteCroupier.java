package com.casino.roulette.croupier;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.dealer.Croupier;
import com.casino.roulette.player.RoulettePlayer;

public interface RouletteCroupier extends Croupier {

	void handleSinglePlayerSpinRequest(RoulettePlayer player, UUID spinId);

	void handleChipAddition(RoulettePlayer player, Integer betPosition, BigDecimal bet);

	void handleChipRemoval(RoulettePlayer player, Boolean removeAllBets);// all chips or last added chip

	void handleChipsRemovalFromPosition(RoulettePlayer player, Integer position);

	void handlePreviousRoundChipsRepetition(RoulettePlayer player);

}
