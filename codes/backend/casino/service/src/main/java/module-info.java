module casino.service {
	exports com.casino.service to casino.web;

	requires casino.blackjack;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires casino.common;
	requires java.logging;
}
