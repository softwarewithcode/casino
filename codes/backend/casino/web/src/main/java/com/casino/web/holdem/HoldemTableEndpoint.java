package com.casino.web.holdem;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.reload.Reload;
import com.casino.poker.export.NoLimitTexasHoldemAPI;
import com.casino.service.table.TexasHoldemCashTableService;
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

@ServerEndpoint(value = "/texas_holdem/{tableId}", decoders = { HoldemDecoder.class }, encoders = { HoldemEncoder.class }, configurator = CommonConfigurator.class)
public class HoldemTableEndpoint extends Endpoint {
	private static final Logger LOGGER = Logger.getLogger(HoldemTableEndpoint.class.getName());

	@Inject
	private TexasHoldemCashTableService holdemService;

	private NoLimitTexasHoldemAPI tableAPI;

	@OnOpen
	public void onOpen(Session session, @PathParam("tableId") String tableId) {
		try {
			super.onOpen(tableId);
			holdemService.fetchTable(super.getTableId()).ifPresentOrElse(this::assignAPI, () -> closeOnOpen(session));
			session.setMaxIdleTimeout(4 * 60 * 1000);
			session.setMaxTextMessageBufferSize(1024 * 2);
			session.setMaxBinaryMessageBufferSize(0);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onOpen error ", e);
		}
	}

	private void assignAPI(NoLimitTexasHoldemAPI api) {
		this.tableAPI = api;
	}

	private void closeOnOpen(Session session) {
		this.onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, " invalid table "));
	}

	@OnMessage
	public void onMessage(Session session, HoldemMessage message) {
		LOGGER.fine("endpoint got message: " + "\n-> message:" + message);
		try {
			validateMessageExist(message);
			if (!isUserConnected(session))
				super.buildAndConnectBridge(session);
			switch (message.getAction()) {
			case OPEN_TABLE -> tableAPI.watch(user);
			case JOIN -> {
				if (!tableAPI.join(user, message.getSeat(), false))
					session.getBasicRemote().sendText("{\"title\":\"FORBIDDEN\"}");
			}
			case ALL_IN -> tableAPI.allIn(user.userId());
			case BET_RAISE -> tableAPI.raiseTo(user.userId(), message.getAmount());
			case CALL -> tableAPI.call(user.userId());
			case CHECK -> tableAPI.check(user.userId());
			case FOLD -> tableAPI.fold(user.userId());
			case LEAVE -> tableAPI.leave(user.userId());
			case REFRESH -> tableAPI.refresh(user.userId());
			case RELOAD_CHIPS -> reloadMinBuyIn();
			case CONTINUE_GAME -> tableAPI.continueGame(user.userId());
			case SIT_OUT_NEXT_HAND -> tableAPI.sitOutNextHand(user.userId());
			default -> throw new IllegalArgumentException("Unexpected value: " + message.getAction());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "HoldemEndpoint: onMessage error:" + user, e);
		}
	}

	private void reloadMinBuyIn() {
		BigDecimal withdrawAmount = super.tryWithdrawFromWallet(tableAPI.getTableCard().getGameData().getMinBuyIn());
		UUID reloadId = UUID.randomUUID();
		CompletableFuture<Reload> reload = tableAPI.reload(user.userId(), reloadId, withdrawAmount);
		try {
			reload.thenAccept(super::finalizeReload);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "ReloadError, Calling  manager for assistance!! User = Softwarewithcode from GitHub " + reload);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		try {
			LOGGER.fine("Closing session:" + closeReason);
			closeChannels(session, tableAPI);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "HoldemEndpoint: onClose error,", e);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOGGER.log(Level.SEVERE, "HoldemEndpoint: onError ", throwable);
		try {
			closeChannels(session, tableAPI);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "OnError, got error in error", e);
		}
	}

}
