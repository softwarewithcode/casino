package com.casino.web.common;

import jakarta.websocket.server.ServerEndpointConfig;

public class CommonConfigurator extends ServerEndpointConfig.Configurator {

	@Override
	public boolean checkOrigin(String originHeaderValue) {
		return true;
	}
}