package com.casino.web.blackjack;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.export.BlackjackTableAPI;
import com.casino.service.table.BlackjackTableService;
import com.casino.web.common.CommonConfigurator;
import com.casino.web.common.Endpoint;

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/blackjack/{tableId}", decoders = { BlackjackMessageDecoder.class }, encoders = { BlackjackEncoder.class }, configurator = CommonConfigurator.class)
public class BlackjackTableEndpoint extends Endpoint {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTableEndpoint.class.getName());
	@Inject
	private BlackjackTableService blackjackTableService;

	private BlackjackTableAPI tableAPI;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId) {
		try {
			super.onOpen(tableId);
			blackjackTableService.fetchTable(super.getTableId()).ifPresentOrElse(this::assignAPI, () -> closeOnOpen(session));
			session.setMaxIdleTimeout(4 * 60 * 1000);
			session.setMaxTextMessageBufferSize(1024 * 2);
			session.setMaxBinaryMessageBufferSize(0);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	private void assignAPI(BlackjackTableAPI api) {
		this.tableAPI = api;
	}

	private void closeOnOpen(Session session) {
		this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
	}

	@OnMessage
	public void onMessage(Session session, BlackjackMessage message) {
		LOGGER.fine("endpoint got message: " + "\n-> message:" + message);
		try {
			validateMessageExist(message);
			if (!isUserConnected(session))
				super.buildAndConnectBridge(session);
			switch (message.getAction()) {
			case OPEN_TABLE -> tableAPI.watch(user);
			case JOIN -> {
				if (!tableAPI.join(user, message.getSeat()))
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
			}
			case BET -> tableAPI.bet(user.userId(), message.getAmount());
			case TAKE -> tableAPI.hit(user.userId());
			case SPLIT -> tableAPI.split(user.userId());
			case DOUBLE_DOWN -> tableAPI.doubleDown(user.userId());
			case STAND -> tableAPI.stand(user.userId());
			case INSURE -> tableAPI.insure(user.userId());
			case REFRESH -> tableAPI.refresh(user.userId());
			default -> throw new IllegalArgumentException("Unexpected messageAction: " + message.getAction());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onMessage error:" + user, e);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.fine("Closing session:" + closeReason);
			closeChannels(session, tableAPI);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onClose error,", e);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onError ", throwable);
		try {
			closeChannels(session, tableAPI);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "OnError, got error in error", e);
		}
	}
}
