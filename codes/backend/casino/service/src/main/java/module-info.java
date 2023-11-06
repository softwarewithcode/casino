module casino.service {
	exports com.casino.service.table to casino.web;
	exports com.casino.service.user to casino.web;
	requires transitive casino.blackjack;
	requires transitive casino.texasholdem;
	requires transitive casino.roulette;
	requires transitive casino.common;
	requires transitive casino.math;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires java.logging;

}
