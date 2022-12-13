package com.casino.web.endpoint.handler;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.user.Bridge;
import com.casino.common.validaton.Validator;

import jakarta.enterprise.context.Dependent;
import jakarta.websocket.Session;

@Dependent
public class UserHandler {

	public Bridge createBridge(String userId, UUID tableId, Session session) {
		if (userId == null || userId.isBlank())
			return createTempUser(tableId, session);
		UUID validUserId = Validator.validateId(userId);
		return fetchUserDataLikeBalanceFromDB(validUserId, tableId, session);
	}

	private Bridge fetchUserDataLikeBalanceFromDB(UUID userId, UUID tableId, Session session) {
		// TODO Auto-generated method stub
		return new Bridge("authUser", tableId, userId, session, new BigDecimal("10000.0"));

	}

	private Bridge createTempUser(UUID tableId, Session session) {
		// TODO Auto-generated method stub
		return new Bridge("guest", tableId, UUID.randomUUID(), session, new BigDecimal("1000.0"));
	}

}
