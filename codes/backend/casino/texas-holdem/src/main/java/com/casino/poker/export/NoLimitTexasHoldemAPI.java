package com.casino.poker.export;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.casino.common.api.TableAPI;
import com.casino.common.reload.Reload;
import com.casino.common.user.User;

public interface NoLimitTexasHoldemAPI extends TableAPI {
	boolean join(User user, String seatNumber, Boolean waitBigBlind);

	void raiseTo(UUID playerId, BigDecimal amount);

	void allIn(UUID playerId);

	void fold(UUID playerId);

	void call(UUID playerId);

	void check(UUID playerId);

	void sitOutNextHand(UUID playerId);

	void continueGame(UUID playerId);

	boolean toggleWaitBigBlind(UUID playerId);

	CompletableFuture<Reload> reload(UUID playerId, UUID reloadId, BigDecimal amount);

}
