package com.casino.poker.tests;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.casino.poker.dealer.PokerDealer;
import com.casino.poker.player.PokerPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.message.Mapper;
import com.casino.common.runner.CasinoMode;
import com.casino.common.user.User;
import com.casino.poker.actions.PokerActionType;
import com.casino.poker.dealer.HoldemDealer;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import com.casino.poker.table.DealerButton;
import com.casino.poker.table.HoldemTable;

public class DefaultTableTests {
	protected User user;
	protected User user2;
	protected User user3;
	protected User user4;
	protected User user5;
	protected User user6;
	protected User userWithoutMoney;
	protected User userMaximum;
	protected User userMaximum2;
	protected User userWithCents;
	protected User user2WithCents;
	protected User user3WithCents;
	protected User user4WithCents;
	protected User user5WithCents;
	protected User user6WithCents;
	protected HoldemTable table;
	protected Long DEFAULT_ROUND_DELAY_MILLIS = TestHoldemTableFactory.DEFAULT_ROUND_DELAY + 500l;
	protected HoldemDealer dealer;
	protected BigDecimal maximumBalance = HoldemDealer.GLOBAL_RESTRICTION_OF_MAXIMUM_RAISE.add(new BigDecimal("30.00"));
	protected BigDecimal maximumBet = HoldemDealer.GLOBAL_RESTRICTION_OF_MAXIMUM_RAISE;
	protected DealerButton dealerButton;
	private static final List<Card> straightFlush = List.of(Card.of(1, Suit.HEART), Card.of(13, Suit.HEART), Card.of(12, Suit.HEART), Card.of(11, Suit.HEART), Card.of(10, Suit.HEART));
	private static final List<Card> almostStraightFlush = List.of(Card.of(13, Suit.HEART), Card.of(12, Suit.HEART), Card.of(11, Suit.HEART), Card.of(10, Suit.HEART), Card.of(8, Suit.CLUB));

	@BeforeAll
	public static void setup() {
		System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "true");
	}

	@BeforeEach
	public void initTest() {
		try {
			System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "2");
			table = TestHoldemTableFactory.createDefaultTexasHoldemCashGameTable();
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			user3 = new User("JamesDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("800"));
			user4 = new User("JackieDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("700"));
			user5 = new User("JillDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("600"));
			user6 = new User("JimDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("900"));
			userWithCents = new User("JoseDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("203.27000000"));
			user2WithCents = new User("JosephineDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("402.191345"));
			user3WithCents = new User("Joanna", table.getId(), UUID.randomUUID(), null, new BigDecimal("402.199345"));
			user4WithCents = new User("JaxonDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("551.457289"));
			user5WithCents = new User("JuliaDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("402.4541345"));
			user6WithCents = new User("JasonDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("556.4553289"));
			userWithoutMoney = new User("brokeDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("0"));
			userMaximum = new User("maximumDoe", table.getId(), UUID.randomUUID(), null, maximumBalance);
			userMaximum2 = new User("maximumDoe2", table.getId(), UUID.randomUUID(), null, maximumBalance);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (HoldemDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected PokerDealer getDefaultDealer() {
		return table.getDealer();
	}

	protected PokerPlayer getDefaultTableSmallBlindPlayer() {
		return table.getRound().getSmallBlindPlayer();
	}

	protected PokerPlayer getDefaultTableBigBlindPlayer() {
		return table.getRound().getBigBlindPlayer();
	}

	protected void defaultJoinJoin() {
		table.join(user, "2", false);
		table.join(user2, "3", false);
		waitRoundToStart();
	}

	protected void defaultJoinJoinJoin() {
		table.join(user, "2", false);
		table.join(user2, "3", false);
		table.join(user3, "4", false);
		waitRoundToStart();
	}
	protected void defaultJoinJoinJoinJoin() {
		table.join(user, "2", false);
		table.join(user2, "3", false);
		table.join(user3, "4", false);
		table.join(user4, "5", false);
		waitRoundToStart();
	}
	protected void maximumJoinJoin() {
		table.join(userMaximum, "3", false);
		table.join(userMaximum2, "2", false);
		waitRoundToStart();
	}

	protected void default6PlayersJoin6MaxTable() {
		table.join(user, "0", true);
		table.join(user2, "1", true);
		table.join(user3, "2", true);
		table.join(user4, "3", true);
		table.join(user5, "4", true);
		table.join(user6, "5", true);
		waitRoundToStart();
	}

	protected void centPlayersJoinPlayersJoin6MaxTable() {
		table.join(userWithCents, "0", true);
		table.join(user2WithCents, "1", true);
		table.join(user3WithCents, "2", true);
		table.join(user4WithCents, "3", true);
		table.join(user5WithCents, "4", true);
		table.join(user6WithCents, "5", true);
		waitRoundToStart();
	}

	protected void defaultJoinJoinCall() {
		defaultJoinJoin();
		table.call(table.getRound().getSmallBlindPlayer().getId());
	}

	protected void waitRoundToStart() {
		sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
	}

	protected void defaultTableCallCheckToFlop() {
		defaultJoinJoinCall();
		table.check(getDefaultTableBigBlindPlayer().getId());
	}

	protected void defaultTableCheckToTurn() {
		defaultTableCallCheckToFlop();
		table.check(table.getRound().getPositions().bb().getId());
		table.check(table.getRound().getPositions().sb().getId());
	}

	protected void defaultTableCheckToRiver() {
		defaultTableCheckToTurn();
		table.check(table.getRound().getPositions().bb().getId());
		table.check(table.getRound().getPositions().sb().getId());
	}

	protected void defaultTableCheckRoundThrough() {
		defaultTableCheckToRiver();
		table.check(table.getRound().getPositions().bb().getId());
		table.check(table.getRound().getPositions().sb().getId());
	}

	protected void sleep(long i, ChronoUnit unit) {
		try {
			Thread.sleep(Duration.of(i, unit));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void everybodyMakeAction(HoldemTable table, PokerActionType type) {
		var actor = table.getActivePlayer();
		for (int i = 0; i < table.getRound().getPlayers().size(); i++) {
			switch (type) {
			case ALL_IN -> table.allIn(actor.getId());
			case CHECK -> table.check(actor.getId());
			}
			actor = table.getActivePlayer();
		}
	}

	protected void assignIrrevelantCardsForPlayers(HoldemTable defaultTable) {
		PokerPlayer p0 = defaultTable.getPlayer(0); // 900
		PokerPlayer p1 = defaultTable.getPlayer(1); // 600
		PokerPlayer p2 = defaultTable.getPlayer(2);// 1000
		PokerPlayer p3 = defaultTable.getPlayer(3);// 1000
		PokerPlayer p4 = defaultTable.getPlayer(4); // 800
		PokerPlayer p5 = defaultTable.getPlayer(5); // 700

		p0.getHoleCards().clear();
		p0.getHoleCards().add(Card.of(1, Suit.SPADE));
		p0.getHoleCards().add(Card.of(2, Suit.CLUB));

		p1.getHoleCards().add(Card.of(1, Suit.HEART));
		p1.getHoleCards().add(Card.of(3, Suit.CLUB));

		p2.getHoleCards().clear();
		p2.getHoleCards().add(Card.of(5, Suit.CLUB));
		p2.getHoleCards().add(Card.of(2, Suit.SPADE));

		p3.getHoleCards().clear();
		p3.getHoleCards().add(Card.of(1, Suit.DIAMOND));
		p3.getHoleCards().add(Card.of(4, Suit.CLUB));

		p4.getHoleCards().clear();
		p4.getHoleCards().add(Card.of(9, Suit.HEART));
		p4.getHoleCards().add(Card.of(2, Suit.DIAMOND));

		p5.getHoleCards().clear();
		p5.getHoleCards().add(Card.of(7, Suit.DIAMOND));
		p5.getHoleCards().add(Card.of(10, Suit.DIAMOND));
	}

	protected void assignIrrelevantTableCards() {
		Card c1 = Card.of(2, Suit.SPADE);
		Card c2 = Card.of(3, Suit.HEART);
		Card c3 = Card.of(5, Suit.DIAMOND);
		Card c4 = Card.of(6, Suit.CLUB);
		Card c5 = Card.of(7, Suit.SPADE);
		List<Card> tableCards = List.of(c1, c2, c3, c4, c5);
		table.getRound().setTableCards(tableCards);
	}

	protected void assignIrrelevantCardsForPlayer(PokerPlayer player) {
		Card c1 = Card.of(2, Suit.CLUB);
		Card c2 = Card.of(3, Suit.DIAMOND);
		player.getHoleCards().clear();
		player.addHoleCard(c1);
		player.addHoleCard(c2);
	}

	protected void assignRoyalFlushFillingCardsForPlayer(PokerPlayer player) {
		player.getHoleCards().clear();
		player.addHoleCard(Card.of(1, Suit.HEART));
		player.addHoleCard(Card.of(2, Suit.HEART));
	}

	protected void assignAlmostStraightFlushTableCards() {
		table.getRound().setTableCards(almostStraightFlush);
	}

	protected void assignStraightFlushTableCards() {
		table.getRound().setTableCards(straightFlush);
	}

	protected void setupCardsForWinnerAndLoser(PokerPlayer winner, PokerPlayer loser) {
		assignIrrelevantCardsForPlayer(loser);
		assignRoyalFlushFillingCardsForPlayer(winner);
		assignAlmostStraightFlushTableCards();
	}

	protected void setupSplitPot() {
		assignStraightFlushTableCards();
		assignIrrevelantCardsForPlayers(table);
	}

	protected void setupDefaultHeadsUpSplitPot() {
		assignStraightFlushTableCards();
		assignIrrelevantCardsForPlayer(table.getPlayer(2));
		assignIrrelevantCardsForPlayer(table.getPlayer(3));
	}
}
