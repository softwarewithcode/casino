package com.casino.common.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.websocket.Session;

import java.math.BigDecimal;
import java.util.UUID;

public record User(
        String userName,
        UUID tableId,
        @JsonIgnore UUID userId,
        @JsonIgnore Session session,
        BigDecimal initialBalance) implements Connectable {
    @Override
    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}
