package com.casino.roulette.export;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.api.TableAPI;
import com.casino.common.user.User;

public interface RouletteTableAPI extends TableAPI {

	boolean join(User user);

	void bet(UUID playerId, Integer position, BigDecimal amount);

	void removeBets(UUID playerId, Boolean removeAll);

	void removeBetsFromPosition(UUID playerId, Integer position);

	// For singlePlayer table playButton
	void play(UUID playerId, UUID spinId);

	void repeatLastBets(UUID playerId);
	
}
