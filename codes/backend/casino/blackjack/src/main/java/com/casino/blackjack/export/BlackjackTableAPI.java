package com.casino.blackjack.export;

import com.casino.common.api.TableAPI;
import com.casino.common.user.User;

import java.math.BigDecimal;
import java.util.UUID;

public interface BlackjackTableAPI extends TableAPI {
    boolean join(User user, String seatNumber);

    void bet(UUID playerId, BigDecimal bet);

    void split(UUID playerId);

    void doubleDown(UUID playerId);

    void hit(UUID playerId);

    void stand(UUID playerId);

    void insure(UUID playerId);

    void refresh(UUID id);  // rename to Options (UUID playerId)?

}
