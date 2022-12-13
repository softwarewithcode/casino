package com.casino.web.endpoint.blackjack;

import jakarta.websocket.server.ServerEndpointConfig;

public class BlackjackConfigurator extends ServerEndpointConfig.Configurator {

	@Override
	public boolean checkOrigin(String originHeaderValue) {
		return true;
	}
}