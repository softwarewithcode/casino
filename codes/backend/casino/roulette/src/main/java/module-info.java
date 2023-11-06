module casino.roulette {
	exports com.casino.roulette.export;
	requires transitive casino.common;
	requires transitive casino.math;
	requires casino.persistence;
	requires jakarta.websocket;
	requires java.logging;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}