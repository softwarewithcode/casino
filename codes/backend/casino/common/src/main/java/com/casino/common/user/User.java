package com.casino.common.user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.websocket.Session;

@JsonIgnoreProperties(value = { "connected", "session", "userId" })
public record User(String userName, UUID tableId, UUID userId, @JsonIgnore Session session, BigDecimal initialBalance) implements Connectable {
	
	public BigDecimal getInitialBalance() { // For serialization
		return initialBalance;
	}

	@JsonIgnore
	@Override
	public boolean isConnected() {
		return session != null && session.isOpen();
	}

	@JsonIgnore
	@Override
	public Session getSession() {
		return session;
	}

	@JsonIgnore
	@Override
	public UUID getId() {
		return userId;
	}

	@Override
	public synchronized <T> void sendMessage(T message) {
		if (!isReachable(message)) 
			return;
		try {
			getSession().getBasicRemote().sendText(message.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private <T> boolean isReachable(T message) {
		return isConnected() && message != null;
	}
}
