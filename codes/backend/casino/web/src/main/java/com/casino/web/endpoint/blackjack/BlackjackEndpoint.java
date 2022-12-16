package com.casino.web.endpoint.blackjack;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.ext.BlackjackReverseProxy;
import com.casino.blackjack.ext.BlackjackTableService;
import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;
import com.casino.common.web.Message;
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
	BlackjackTableService tableServiceTemp = new BlackjackTableService();

	private UUID tableId;
	private BlackjackReverseProxy proxy;
	private Bridge bridge;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId, EndpointConfig ec) {
		System.out.println("onOpen called " + tableId);
		UUID id = Validator.validateId(tableId);
		BlackjackReverseProxy proxy = tableServiceTemp.getTable(id);
		if (proxy == null) {
			tableId = null;
			bridge = null;
			this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
			return;
		}
		this.tableId = id;
		this.proxy = proxy;
	}

	@OnMessage
	public void onMessage(Session session, Message message) {
		LOGGER.info("endpoint got message: " + tableId + "\n-> message:" + message);
		if (isFirstMessage(session)) {
			createBridge(session, message);
			proxy.join(bridge, "0");
			return;
		}
		if (message.getAction() == null)
			throw new IllegalArgumentException("action is missing");
		try {
			switch (message.getAction()) {
			case BET -> proxy.bet(bridge.playerId(), message.getAmount());
			case TAKE -> proxy.hit(bridge.playerId());
			case SPLIT -> proxy.split(bridge.playerId());
			case DOUBLE_DOWN -> proxy.doubleDown(bridge.playerId());
			case STAND -> proxy.stand(bridge.playerId());
			case INSURE -> proxy.insure(bridge.playerId());
			default -> LOGGER.severe("action is missing," + bridge);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Proxy error:" + bridge, e);
		}
		System.out.println("Command");
	}

	private void createBridge(Session session, Message message) {
		this.bridge = userHandler.createBridge(message.getUserId(), tableId, session);
	}

	private boolean isFirstMessage(Session session) {
		return this.bridge == null && session.isOpen();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.info("Closing session:" + closeReason);
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
