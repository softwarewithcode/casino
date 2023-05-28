module casino.web {
	exports com.casino.web.blackjack;
	exports com.casino.web.holdem;
    exports com.casino.web.common;

    requires transitive casino.service;
	requires transitive casino.common;
	requires transitive jakarta.websocket;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.json.bind;
	requires jakarta.servlet;
	requires jakarta.ws.rs;
	requires java.logging;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}