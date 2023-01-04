package com.casino.common.user;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.websocket.Session;

public record Bridge(String name, UUID tableId, @JsonIgnore UUID userId, @JsonIgnore Session session, BigDecimal initialBalance) {

	public boolean isConnected() {
		return session != null && session.isOpen();
	}
}
