package com.casino.web.common;

import com.casino.common.api.TableAPI;
import com.casino.common.reload.Reload;
import com.casino.common.user.User;
import com.casino.common.validation.Verifier;
import com.casino.service.user.UserService;

import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public class Endpoint {

	@Inject
	private UserService userService;
	protected User user;
	protected UUID tableId;

	protected void validateMessageExist(Message message) {
		if (!containsMessage(message))
			throw new IllegalArgumentException("CommonEndpoint: message is missing");
	}

	protected boolean containsMessage(Message message) {
		return message != null && message.getAction() != null;
	}

	protected void buildAndConnectBridge(Session session) {
		user = userService.createGuestPlayerBridge(tableId, session);
	}

	protected void closeChannels(Session session, TableAPI api) throws IOException {
		api.leave(user.userId());
		tearDown();
		session.close();
	}

	protected boolean isUserConnected(Session session) {
		return this.user != null && session.isOpen();
	}

	public void setTableId(UUID tableId) {
		this.tableId = tableId;
	}

	protected void onOpen(String tableId2) {
		UUID id = Verifier.verifyIdStructure(tableId2);
		this.tableId = id;
	}

	protected void tearDown() {
		this.tableId = null;
		this.user = null;
	}

	public User getBridge() {
		return user;
	}

	public UUID getTableId() {
		return tableId;
	}

	protected BigDecimal tryWithdrawFromWallet(BigDecimal amount) {
		return userService.withdrawFromWallet(user.userId(), amount);
	}

	protected void finalizeReload(Reload finishedReload) {
		userService.finalizeReload(finishedReload);
	}
}
