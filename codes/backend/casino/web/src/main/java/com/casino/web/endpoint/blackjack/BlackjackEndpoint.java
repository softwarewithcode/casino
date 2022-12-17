package com.casino.web.endpoint.blackjack;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.blackjack.ext.IBlackjackTable;
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
	private BlackjackTableService tableService = new BlackjackTableService();

	private IBlackjackTable table;
	private Bridge bridge;
	private UUID tableId;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId, EndpointConfig ec) {
		System.out.println("onOpen called " + tableId);
		try {
			UUID id = Validator.validateId(tableId);
			Optional<IBlackjackTable> table = tableService.fetchTable(id);
			if (table.isEmpty()) {
				tableId = null;
				bridge = null;
				this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
				return;
			}
			this.tableId = id;
			this.table = table.get();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	@OnMessage
	public void onMessage(Session session, Message message) {
		LOGGER.info("endpoint got message: " + "\n-> message:" + message);
		try {
			if (isFirstMessage(session)) {
				createBridge(session, message);
				table.join(bridge, "3");
				return;
			}
			if (message.getAction() == null)
				throw new IllegalArgumentException("action is missing");
			switch (message.getAction()) {
			case BET -> table.bet(bridge.playerId(), message.getAmount());
			case TAKE -> table.hit(bridge.playerId());
			case SPLIT -> table.split(bridge.playerId());
			case DOUBLE_DOWN -> table.doubleDown(bridge.playerId());
			case STAND -> table.stand(bridge.playerId());
			case INSURE -> table.insure(bridge.playerId());
			case REFRESH -> {
//				bridge.session().getBasicRemote()
			}
			default -> LOGGER.severe("action is missing," + bridge);
			}
			System.out.println("Command ok");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onMessage error:" + bridge, e);
		}
	}

	private void createBridge(Session session, Message message) {
		this.bridge = userHandler.createBridge(message.getUserId(), this.tableId, session);
	}

	private boolean isFirstMessage(Session session) {
		return this.bridge == null && session.isOpen();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.info("Closing session:" + closeReason);
			if (bridge != null && bridge.playerId() != null)
				table.onPlayerLeave(bridge.playerId());
			session.close(closeReason);// can close session here, table checks that this session is not open anymore
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {

	}
}
