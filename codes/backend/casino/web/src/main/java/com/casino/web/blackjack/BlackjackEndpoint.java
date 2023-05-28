package com.casino.web.blackjack;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.export.BlackjackAPI;
import com.casino.service.game.BlackjackService;
import com.casino.web.common.CommonConfigurator;
import com.casino.web.common.CommonEndpoint;

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
public class BlackjackEndpoint extends CommonEndpoint {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEndpoint.class.getName());
	@Inject
	private BlackjackService blackjackService;

	private BlackjackAPI tableAPI;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId) {
		try {
			super.onOpen(tableId);
			blackjackService.fetchTable(super.getTableId()).ifPresentOrElse(this::assignAPI, () -> closeOnOpen(session));
			session.setMaxIdleTimeout(4 * 60 * 1000);
			session.setMaxTextMessageBufferSize(1024 * 2);
			session.setMaxBinaryMessageBufferSize(0);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	private void assignAPI(BlackjackAPI api) {
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
			if (!isBridgeConnecting(session))
				super.buildAndConnectBridge(session);
			switch (message.getAction()) {
			case OPEN_TABLE -> tableAPI.watch(bridge);
			case JOIN -> {
				if (!tableAPI.join(bridge, message.getSeat()))
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
			}
			case BET -> tableAPI.bet(bridge.userId(), message.getAmount());
			case TAKE -> tableAPI.hit(bridge.userId());
			case SPLIT -> tableAPI.split(bridge.userId());
			case DOUBLE_DOWN -> tableAPI.doubleDown(bridge.userId());
			case STAND -> tableAPI.stand(bridge.userId());
			case INSURE -> tableAPI.insure(bridge.userId());
			case REFRESH -> tableAPI.refresh(bridge.userId());
			default -> throw new IllegalArgumentException("Unexpected messageAction: " + message.getAction());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onMessage error:" + bridge, e);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.fine("Closing session:" + closeReason);
			tableAPI.leave(bridge.userId());
			tearDown();
			session.close();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onClose error,", e);
		}
	}



	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onError ", throwable);
		try {
			session.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "OnError, got error in error", e);
		}
	}
}
