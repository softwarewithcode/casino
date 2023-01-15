package com.casino.web.socket.blackjack;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;
import com.casino.common.web.Message;
import com.casino.service.game.GameService;
import com.casino.service.user.UserService;

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/blackjack/{tableId}", decoders = { BlackjackMessageDecoder.class }, encoders = { BlackjackEncoder.class }, configurator = BlackjackConfigurator.class)
public class BlackjackEndpoint {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEndpoint.class.getName());
	@Inject
	private UserService userService;
	@Inject
	private GameService blackjackService;

	private IBlackjackTable table;
	private Bridge bridge;
	private UUID tableId;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId) {
		try {
			UUID id = Validator.validateId(tableId);
			Optional<IBlackjackTable> table = blackjackService.fetchTable(id);
			if (table.isEmpty()) {
				this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
				return;
			}
			this.tableId = id;
			this.table = table.get();
			session.setMaxIdleTimeout(30 * 60 * 1000);
			session.setMaxTextMessageBufferSize(1024 * 2);
			session.setMaxBinaryMessageBufferSize(0);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	@OnMessage
	public void onMessage(Session session, Message message) {
		LOGGER.fine("endpoint got message: " + "\n-> message:" + message);
		try {
			if (isFirstMessage(session)) {
				createBridge(session, message);
			}
			validateMessageAndBridge(message);
			switch (message.getAction()) {
			case OPEN_TABLE -> table.watch(bridge);
			case JOIN -> {
				if (!table.join(bridge, message.getSeat()))
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
			}
			case BET -> table.bet(bridge.userId(), message.getAmount());
			case TAKE -> table.hit(bridge.userId());
			case SPLIT -> table.split(bridge.userId());
			case DOUBLE_DOWN -> table.doubleDown(bridge.userId());
			case STAND -> table.stand(bridge.userId());
			case INSURE -> table.insure(bridge.userId());
			case REFRESH -> table.refresh(bridge.userId());
			default -> throw new IllegalArgumentException("Unexpected messageAction: " + message.getAction());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onMessage error:" + bridge, e);
		}
	}

	private void validateMessageAndBridge(Message message) {
		if (!containsMessage(message))
			throw new IllegalArgumentException("BlackjackEndpoint: action is missing");
		if (bridge == null || bridge.userId() == null)
			throw new IllegalArgumentException("No bridge");
		if (table == null)
			throw new IllegalArgumentException("Not related to real table");
	}

	private boolean containsMessage(Message message) {
		return message.getAction() != null;
	}

	private void createBridge(Session session, Message message) {
		this.bridge = userService.createGuestPlayerBridge(message.getUserId(), this.tableId, session);
	}

	private boolean isFirstMessage(Session session) {
		return this.bridge == null && session.isOpen();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.fine("Closing session:" + closeReason);
			session.close();
			blackjackService.onSocketClose(bridge);
			this.table = null;
			this.tableId = null;
			this.bridge = null;
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
