package com.casino.web.endpoint.handler;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;

import jakarta.enterprise.context.Dependent;
import jakarta.websocket.Session;

@Dependent
public class UserService {

	public Bridge createPlayerBridge(String userId, UUID tableId, Session session) {
		if (userId == null || userId.isBlank())
			return createDefaultGuestPlayerBridge(tableId, session);
		UUID validUserId = Validator.validateId(userId);
		return fetchUserDataLikeBalanceFromDB(validUserId, tableId, session);
	}

	private AtomicInteger guestCount = new AtomicInteger();

	public Bridge createGuestPlayerBridge(String userId, UUID tableId, Session session) {
		return createDefaultGuestPlayerBridge(tableId, session);
	}

	private Bridge fetchUserDataLikeBalanceFromDB(UUID userId, UUID tableId, Session session) {
		// TODO Auto-generated method stub
		return new Bridge("authUser", tableId, userId, session, new BigDecimal("10000.0"));
	}

	private Bridge createDefaultGuestPlayerBridge(UUID tableId, Session session) {
		UUID id = UUID.randomUUID();
		int guestNumber = guestCount.addAndGet(1);
		return new Bridge("guest" + guestNumber, tableId, id, session, new BigDecimal("1000.0"));
	}
}
