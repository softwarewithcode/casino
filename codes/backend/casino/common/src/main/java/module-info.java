module casino.common {
	exports com.casino.common.validaton;
	exports com.casino.common.cards;
	exports com.casino.common.exception;
	exports com.casino.common.web;
	exports com.casino.common.player;
	exports com.casino.common.table.phase;
	exports com.casino.common.language;
	exports com.casino.common.table;
	exports com.casino.common.user;
	exports com.casino.common.bet;
	exports com.casino.common.common;
	exports com.casino.common.table.timing;
	requires jakarta.json.bind;
	requires jakarta.websocket;
	requires java.logging;
	requires com.fasterxml.jackson.databind;
}