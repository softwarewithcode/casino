package com.casino.poker.export;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.casino.common.api.BaseTableAPI;
import com.casino.common.reload.Reload;
import com.casino.common.user.Bridge;

public interface NoLimitTexasHoldemAPI extends BaseTableAPI {
	boolean join(Bridge user, String seatNumber, Boolean waitBigBlind);

	void raiseTo(UUID playerId, BigDecimal amount);

	void allIn(UUID playerId);

	void fold(UUID playerId);

	void call(UUID playerId);

	void check(UUID playerId);

	void leave(UUID playerId);

	void refresh(UUID playerId); // rename to Options (UUID playerId)?

	void sitOutNextHand(UUID playerId);

	void continueGame(UUID playerId);

	boolean toggleWaitBigBlind(UUID playerId);

	CompletableFuture<Reload> reload(UUID playerId, UUID reloadId, BigDecimal amount);

}
