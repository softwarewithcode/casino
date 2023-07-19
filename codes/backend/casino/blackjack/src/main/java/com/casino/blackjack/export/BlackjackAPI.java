package com.casino.blackjack.export;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.api.BaseTableAPI;

public interface BlackjackAPI extends BaseTableAPI {

	void bet(UUID playerId, BigDecimal bet);

	void split(UUID playerId);

	void doubleDown(UUID playerId);

	void hit(UUID playerId);

	void stand(UUID playerId);

	void insure(UUID playerId);

	void leave(UUID playerId);

	void refresh(UUID id);  // rename to Options (UUID playerId)?

}
