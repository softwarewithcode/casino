package com.casino.common.user;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.websocket.Session;

public record Bridge(
		String userName,
		@JsonIgnore UUID userId, 
		UUID tableId, 
		@JsonIgnore Session session, 
		BigDecimal initialBalance) {

	public boolean isConnected() {
		return session != null && session.isOpen();
	}
}
