module casino.blackjack {
	exports com.casino.blackjack.ext;
	exports com.casino.blackjack.table to casino.service;

	requires casino.common;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires jakarta.websocket;
	requires java.logging;
}