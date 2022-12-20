module casino.service {
	exports com.casino.service to casino.web;

	requires casino.blackjack;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires transitive casino.common;
	requires java.logging;

}
