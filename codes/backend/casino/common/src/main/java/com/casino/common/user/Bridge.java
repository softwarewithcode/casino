package com.casino.common.user;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.websocket.Session;

public record Bridge(String name, UUID tableId, UUID userId, Session session, BigDecimal initialBalance) {

	public boolean isConnectable() {
		return session != null && session.isOpen();
	}
}
