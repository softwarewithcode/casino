package com.casino.web.endpoint.handler;

import java.util.UUID;

import com.casino.blackjack.ext.BlackjackTableService;
import com.casino.blackjack.ext.BlackjackReverseProxy;

import jakarta.enterprise.context.Dependent;

@Dependent
public class TableHandler {

	public BlackjackReverseProxy fetchTable(UUID tableId) {
		BlackjackTableService temp = new BlackjackTableService();
		return temp.getTable(tableId);
	}
}
