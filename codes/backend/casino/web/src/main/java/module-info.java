module casino.web {
	exports com.casino.web.endpoint.blackjack;
	exports com.casino.web.endpoint.handler;

	requires casino.blackjack;
	requires casino.common;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires jakarta.websocket;
	requires java.logging;
}