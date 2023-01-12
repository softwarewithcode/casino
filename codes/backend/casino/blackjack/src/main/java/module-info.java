module casino.blackjack {
	exports com.casino.blackjack.ext;
	exports com.casino.blackjack.table to casino.service;

	requires transitive casino.common;
	requires jakarta.websocket;
	requires java.logging;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}