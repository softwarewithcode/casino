package com.casino.web.endpoint.blackjack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.common.user.Action;
import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;
import com.casino.common.web.Message;
import com.casino.service.BlackjackTableService;
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

@ServerEndpoint(value = "/blackjack/{tableId}", decoders = { BlackjackMessageDecoder.class }, encoders = { BlackjackEncoder.class }, configurator = BlackjackConfigurator.class)
public class BlackjackEndpoint {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEndpoint.class.getName());
	@Inject
	private UserHandler userHandler;
	@Inject
	private BlackjackTableService tableService;

	private IBlackjackTable table;
	private Bridge bridge;
	private UUID tableId;
	private boolean watcher;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId, EndpointConfig ec) {
		System.out.println("onOpen called " + tableId);
		try {
			UUID id = Validator.validateId(tableId);
			handlePossibleWatcherParam(session);
			Optional<IBlackjackTable> table = tableService.fetchTable(id);
			if (table.isEmpty()) {
				tableId = null;
				bridge = null;
				this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
				return;
			}
			this.tableId = id;
			this.table = table.get();
//			this.table.watch(bridge);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	private void handlePossibleWatcherParam(Session session) {
		List<String> watch = session.getRequestParameterMap().get("watch");
		if (watch != null) {
			String watchParam = watch.get(0);
			Integer watching = Integer.parseInt(watchParam);
			if (watching != 1)
				throw new IllegalArgumentException("watch param exist but is not what is expected");
//			this.watcher = true;
		}
	}

	@OnMessage
	public void onMessage(Session session, Message message) {
		LOGGER.info("endpoint got message: " + "\n-> message:" + message);
		try {
			if (isFirstMessage(session)) {
				createBridge(session, message);
			}
			validateMessageAndBridge(message);
			switch (message.getAction()) {
			case OPEN_TABLE -> {
				watcher = true;
				if (!table.watch(bridge)) {
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
				}
			}
			case JOIN -> {
				boolean player = table.join(bridge, message.getSeat());
				if (player)
					watcher = false;
				else
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
			}
			case BET -> table.bet(bridge.userId(), message.getAmount());
			case TAKE -> table.hit(bridge.userId());
			case SPLIT -> table.split(bridge.userId());
			case DOUBLE_DOWN -> table.doubleDown(bridge.userId());
			case STAND -> table.stand(bridge.userId());
			case INSURE -> table.insure(bridge.userId());
			case REFRESH -> table.refresh(bridge.userId());
			}
			System.out.println("Command ok " + message.getAction());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onMessage error:" + bridge, e);
//			this.onClose(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION);
		}
	}

	private void validateMessageAndBridge(Message message) {
		if (!containsMessage(message))
			throw new IllegalArgumentException("BlackjackEndpoint: action is missing");
//		if (watcher && message.getAction() != Action.JOIN)
//			throw new IllegalArgumentException("Watcher cannot play " + bridge);
		if (bridge == null || bridge.userId() == null)
			throw new IllegalArgumentException("Bridge detached");
		if (table == null)
			throw new IllegalArgumentException("Not related to real table");
	}

	private boolean containsMessage(Message message) {
		return message.getAction() != null;
	}

	private void createBridge(Session session, Message message) {
//		if (watcher)
		this.bridge = userHandler.createGuestPlayerBridge(message.getUserId(), this.tableId, session);
//		else
//			this.bridge = userHandler.createPlayerBridge(message.getUserId(), this.tableId, session);
	}

	private boolean isFirstMessage(Session session) {
		return this.bridge == null && session.isOpen();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.info("Closing session:" + closeReason);
			if (shouldCloseImmediately(session))
				session.close();
			if (isPlayerSessionClosing())
				table.onPlayerLeave(bridge.userId());
			else if (isWatcherSessionClosing())
				tableService.removeWatcher(bridge);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onClose error,", e);
		}
	}

	private boolean shouldCloseImmediately(Session session) {
		return this.table == null && session != null && session.isOpen();
	}

	private boolean isWatcherSessionClosing() {
		return bridge != null && bridge.userId() != null && this.watcher == true;
	}

	private boolean isPlayerSessionClosing() {
		return bridge != null && bridge.userId() != null && this.watcher == false;
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.log(Level.SEVERE, "BlackjackEndpoint: onError ", throwable);
	}
}
