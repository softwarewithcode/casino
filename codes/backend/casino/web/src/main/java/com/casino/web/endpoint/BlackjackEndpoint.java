package com.casino.web.endpoint;

import com.casino.common.user.Action;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/blackjack")
public class BlackjackEndpoint {

	@OnOpen
	public void onOpen(Session session) {
	}

	@OnMessage
	public void onMessage(Action action, Session session) {

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
	}

	@OnError
	public void onError(Session session, Throwable throwable) {

	}
}
