package com.casino.common.user;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.websocket.Session;

public record Bridge(String name, UUID tableId, UUID playerId, Session session, BigDecimal initialBalance) {
}
