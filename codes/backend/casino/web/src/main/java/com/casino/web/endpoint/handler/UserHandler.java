package com.casino.web.endpoint.handler;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;

import jakarta.enterprise.context.Dependent;
import jakarta.websocket.Session;

@Dependent
public class UserHandler {

	public Bridge createPlayerBridge(String userId, UUID tableId, Session session) {
		if (userId == null || userId.isBlank())
			return createDefaultGuestPlayerBridge(tableId, session);
		UUID validUserId = Validator.validateId(userId);
		return fetchUserDataLikeBalanceFromDB(validUserId, tableId, session);
	}

	private static int tempNotThreadSafe = 0;

	public Bridge createGuestPlayerBridge(String userId, UUID tableId, Session session) {
		tempNotThreadSafe++;
		return createDefaultGuestPlayerBridge(tableId, session);
	}

	private Bridge fetchUserDataLikeBalanceFromDB(UUID userId, UUID tableId, Session session) {
		// TODO Auto-generated method stub
		return new Bridge("authUser", tableId, userId, session, new BigDecimal("10000.0"));

	}

	private Bridge createDefaultGuestPlayerBridge(UUID tableId, Session session) {
		// TODO Auto-generated method stub
		UUID id = UUID.randomUUID();
		return new Bridge("guestP" + tempNotThreadSafe, tableId, id, session, new BigDecimal("1000.0"));
	}

//	private Bridge createDefaultWatcherBridge(UUID tableId, Session session) {
//		// TODO Auto-generated method stub
//		UUID id = UUID.randomUUID();
//		return new Bridge("watcherGuest" + id, tableId, id, session, new BigDecimal("1000.0"));
//	}
}
