package com.casino.common.user;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.websocket.Session;

public interface Connectable {
	boolean isConnected();

	 <T> void sendMessage(T message);
	@JsonIgnore
	private <T> boolean isReachable(T message) {
		return isConnected() && message != null;
	}

	@JsonIgnore
	Session getSession();

	UUID getId();
}
