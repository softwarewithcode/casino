package com.casino.web.socket.blackjack;

import jakarta.websocket.server.ServerEndpointConfig;

public class BlackjackConfigurator extends ServerEndpointConfig.Configurator {

	@Override
	public boolean checkOrigin(String originHeaderValue) {
		return true;
	}
}