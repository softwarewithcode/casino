module casino.persistence {
	exports com.casino.persistence.export;
	requires transitive java.logging;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}