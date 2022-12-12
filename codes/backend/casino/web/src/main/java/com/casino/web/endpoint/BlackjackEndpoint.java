package com.casino.web.endpoint;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import com.casino.web.endpoint.handler.UserHandler;

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/blackjack/{tableId}")
public class BlackjackEndpoint {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEndpoint.class.getName());
	@Inject
	private UserHandler userHandler;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String id, EndpointConfig ec) {
		UUID tableId = Validator.validateUUID(id);
	}

	@OnMessage
	public void onMessage(BlackjackMessage message, Session session) {

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			session.close(closeReason);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {

	}
}
