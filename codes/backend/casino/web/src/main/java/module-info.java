module casino.web {
	exports com.casino.web.endpoint.blackjack;
	exports com.casino.web.endpoint.handler;

	requires transitive casino.service;
	requires casino.blackjack;
	requires casino.common;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires jakarta.websocket;
	requires jakarta.ws.rs;
	requires java.logging;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires jakarta.servlet;
}