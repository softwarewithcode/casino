module casino.web {
	exports com.casino.web.endpoint;
	exports com.casino.web.endpoint.handler;

	requires casino.common;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.websocket;
	requires java.logging;
	requires casino.blackjack;
}