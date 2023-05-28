module casino.common {
	exports com.casino.common.validation;
	exports com.casino.common.dealer;
	exports com.casino.common.language;
	exports com.casino.common.table.timing;
	exports com.casino.common.cards;
	exports com.casino.common.message;
	exports com.casino.common.exception;
	exports com.casino.common.player;
	exports com.casino.common.game;
	exports com.casino.common.table;
	exports com.casino.common.user;
	exports com.casino.common.bet;
	exports com.casino.common.functions;
	exports com.casino.common.reload;
	exports com.casino.common.table.structure;
	exports com.casino.common.game.phase;
	exports com.casino.common.game.phase.bet;
	exports com.casino.common.game.phase.insurance;
	exports com.casino.common.api;
    exports com.casino.common.action;

    requires transitive com.fasterxml.jackson.annotation;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive jakarta.json.bind;
	requires transitive jakarta.websocket;
	requires java.logging;
}