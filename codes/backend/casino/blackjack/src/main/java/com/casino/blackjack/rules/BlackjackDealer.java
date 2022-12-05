package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.table.BlackjackUtil;
import com.casino.common.bet.BetPhaseClockTask;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Seat;
import com.casino.common.table.phase.GamePhase;

public class BlackjackDealer implements IDealer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());
	private static final BigDecimal BLACKJACK_FACTOR = new BigDecimal("2.5");
	private final BetThresholds betThresholds;
	private final BlackjackTable table;
	private List<Card> decks; // 6 decks
	private BlackjackDealerHand dealerHand;

	public BlackjackDealer(BlackjackTable blackjackTable, BetThresholds betThresholds) {
		this.table = blackjackTable;
		this.betThresholds = betThresholds;
		this.decks = Deck.combineDecks(6);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
	}

	private void startBetPhase() {
		table.updateGamePhase(GamePhase.BET);
		if (table.getActivePlayerCount() > 0) {
			BetPhaseClockTask task = new BetPhaseClockTask(table);
			this.getTable().getClock().startClock(task, 1000);
		}
	}

	public void handlePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateTotalBet(bet, table);
		}, () -> {
			throw new PlayerNotFoundException("Player not found in table:" + table + " player:" + tablePlayer, 1);
		});
	}

	public IHand getHand() {
		return dealerHand;
	}

	public void addCard(Card card) {
		this.dealerHand.addCard(card);
	}

	public void initTable() {
		createDecks();
	}

	private void createDecks() {
		decks = Deck.combineDecks(6);
	}

	public List<Card> getDecks() {
		return decks;
	}

	public BetThresholds getBetThresholds() {
		return betThresholds;
	}

	public BlackjackTable getTable() {
		return table;
	}

	public boolean dealInitialCards() {
		if (!isAllowedToDealStartingHand())
			return false;
		List<ICasinoPlayer> orderedPlayers = getOrderedPlayersWithBet();
		orderedPlayers.forEach(player -> dealCard(player.getHands().get(0))); // first the players
		dealCard(dealerHand); // then dealer
		orderedPlayers.forEach(player -> { // then players again
			dealCard(player.getHands().get(0));
		});
		return true;
	}

	private void dealCard(IHand hand) {
		Card card = decks.remove(decks.size() - 1);
		System.out.println("DEAL:" + card + " hand:");
		hand.addCard(card);
	}

	public void handleAdditionalCard(ICasinoPlayer player) {
		Card card = decks.remove(decks.size() - 1);
		IHand activeHand = getActiveHand(player);
		activeHand.addCard(card);
	}

	private IHand getActiveHand(ICasinoPlayer player) {
		return player.getHands().stream().filter(hand -> !hand.isCompleted()).findFirst().orElseThrow();
	}

	public void handleNewPlayer(ICasinoPlayer player) {
		if (table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS) {
			table.setStatus(com.casino.common.table.Status.RUNNING);
			startBetPhase();
		}
	}

	private boolean isAllowedToDealStartingHand() {
		return table.isGamePhase(GamePhase.BETS_COMPLETED) && somebodyHasBet();
	}

	private boolean somebodyHasBet() {
		return getOrderedPlayersWithBet() != null && getOrderedPlayersWithBet().size() > 0;
	}

	private List<ICasinoPlayer> getOrderedPlayersWithBet() {
		return table.getSeats().stream().filter(seat -> seat.hasPlayerWithBet()).sorted(Comparator.comparing(Seat::getNumber)).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public void finalizeBetPhase() {
		table.getClock().stopClock();
		handlePlayers();
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
	}

	private void handlePlayers() {
		table.getSeats().stream().filter(seat -> seat.hasPlayer()).map(seat -> seat.getPlayer()).forEach(player -> {
			player.setStatus(Status.SIT_OUT);
			if (!player.hasBet())
				table.changeFromPlayerToWatcher(player);
			else {
				player.subtractTotalBetFromBalance();
				player.setStatus(Status.ACTIVE);
				player.getHands().get(0).updateBet(player.getTotalBet());
			}
		});
	}

	public void updateStartingPlayer() {
		Optional<Seat> startingPlayer = table.getSeats().stream().filter(seat -> seat.hasPlayerWithBet()).min(Comparator.comparing(Seat::getNumber));
		if (startingPlayer.isEmpty()) {
			throw new IllegalStateException("Should start playing but no players with bet");
		}
		Seat nextSeat = startingPlayer.get();
		table.updatePlayerInTurn(nextSeat.getPlayer());
	}

	public void changeTurn() {
		Seat nextPlayer = table.getNextSeatWithBet();
		if (nextPlayer != null) {
			table.updatePlayerInTurn(nextPlayer.getPlayer());
			table.updateDealerTurn(false);
		} else {
			table.updateDealerTurn(true);
			table.updatePlayerInTurn(null);
		}
	}

	public void doubleDown(BlackjackPlayer player) {
		Card cardReference = decks.get(decks.size() - 1);
		player.doubleDown(cardReference);
		decks.remove(decks.size() - 1);
	}

	public void handleSplit(BlackjackPlayer player) {
		player.splitStartingHand();
		player.getHands().get(0).addCard(decks.remove(decks.size() - 1));
	}

	public void stand(BlackjackPlayer player) {
		player.stand();
	}

	@Override
	public void completeRound() {
		try {
			if (table.hasWaitingPlayers()) {
				takeCards();
				payout();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something unexpected happend. Waiting for brush to arrive.", e);
			BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
			throw new IllegalStateException("what to do");
		}
	}

	private void payout() {
		List<ICasinoPlayer> playersWaitingPayout = table.getPlayers().stream().filter(player -> player.isWaitingForDealer()).collect(Collectors.toList());
		playersWaitingPayout.stream().forEach(player -> player.getHands().forEach(hand -> {
			int comparison = dealerHand.compareTo(hand);
			if (comparison == 0)
				player.increaseBalance(hand.getBet());
			if (comparison > 0) {
				if (hand.isBlackjack())
					player.increaseBalance(hand.getBet().multiply(BLACKJACK_FACTOR));
				else
					player.increaseBalance(hand.getBet().multiply(BigDecimal.TWO));
			}

			System.out.println("Comparison:" + comparison + " dealer:" + dealerHand + " oterher:" + hand);
		}));
	}

	private void takeCards() {
		while (!dealerHand.isCompleted()) {
			Card card = decks.remove(decks.size() - 1);
			System.out.println("dealer continues and takes card:" + card);
			addCard(card);
			System.out.println("Dealer hand value in first:" + dealerHand.calculateValues().get(0));
		}
		System.out.println("Dealer hand is completed with value:" + dealerHand.calculateValues().get(0));
	}
}
