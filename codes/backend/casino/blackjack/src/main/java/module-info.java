module casino.blackjack {
	exports com.casino.blackjack.ext;

	requires casino.common;
	requires jakarta.websocket;
	requires java.logging;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
}