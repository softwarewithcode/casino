module casino.blackjack {
	exports com.casino.blackjack.player;
	exports com.casino.blackjack.table;
	exports com.casino.blackjack.rules;
	exports com.casino.blackjack.table.timing;

	requires transitive casino.common;
	requires java.logging;
}