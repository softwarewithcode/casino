package com.casino.web.roulette;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.Event;
import com.casino.common.message.Mapper;
import com.casino.roulette.export.RouletteTableAPI;
import com.casino.service.table.RouletteTableService;
import com.casino.web.common.CommonConfigurator;
import com.casino.web.common.Endpoint;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/roulette/{tableId}", decoders = { RouletteDecoder.class }, encoders = { RouletteEncoder.class }, configurator = CommonConfigurator.class)
public class RouletteTableEndpoint extends Endpoint {
	private static final Logger LOGGER = Logger.getLogger(RouletteTableEndpoint.class.getName());
	@Inject
	private RouletteTableService rouletteTableService;

	private RouletteTableAPI tableAPI;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId) {
		try {
			super.onOpen(tableId);
			rouletteTableService.fetchTable(super.getTableId()).ifPresentOrElse(this::assignAPI, () -> closeOnOpen(session));
			session.setMaxIdleTimeout(4 * 60 * 1000);
			session.setMaxTextMessageBufferSize(1024 * 2);
			session.setMaxBinaryMessageBufferSize(0);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	private void assignAPI(RouletteTableAPI api) {
		this.tableAPI = api;
	}

	private void closeOnOpen(Session session) {
		this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
	}

	@OnMessage
	public void onMessage(Session session, RouletteMessage message) {
		LOGGER.fine("endpoint got message: " + "\n-> message:" + message);
		try {
			validateMessageExist(message);
			if (!isUserConnected(session))
				super.buildAndConnectBridge(session);
			switch (message.getAction()) {
			case OPEN_TABLE -> tableAPI.watch(user);
			case JOIN -> {
				try {
					if (!tableAPI.join(user))
						session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
				} catch (Exception i) {
					LOGGER.severe("Player cannot join table " + i.toString());
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
				}
			}
			case BET -> tableAPI.bet(user.userId(), message.getPosition(), message.getAmount());
			case REMOVE_LAST_OR_ALL_BETS -> tableAPI.removeBets(user.userId(), message.getRemoveAllBets());
			case REMOVE_BET_FROM_POSITION -> tableAPI.removeBetsFromPosition(user.userId(), message.getPosition());
			case REFRESH -> tableAPI.refresh(user.userId());
			case PLAY -> tableAPI.play(user.userId(), message.getSpinId());
			case REPEAT_LAST -> tableAPI.repeatLastBets(user.userId());
			case FETCH_BET_POSITIONS -> sendBetPositionMap();
			default -> throw new IllegalArgumentException("Unexpected messageAction: " + message.getAction());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "RouletteEndpoint: onMessage error:" + user, e);
		}
	}

	// Endpoint can send this information since it is static and does not require direct dealer response.
	private void sendBetPositionMap() {
		var message = new RouletteMessage();
		message.setTitle(Event.INIT_DATA);
		try {
			user.sendMessage(Mapper.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(message));
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, " cannot send betPositionMap ", e);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.fine("Closing session:" + closeReason);
			closeChannels(session, tableAPI);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "RouletteEndpoint: onClose error,", e);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.log(Level.SEVERE, "RouletteEndpoint: onError ", throwable);
		try {
			closeChannels(session, tableAPI);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "OnError, got error in error", e);
		}
	}
}