package com.casino.blackjack.rules;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.table.BlackjackUtil;
import com.casino.blackjack.table.timing.InsurancePhaseClockTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.IDealer;
import com.casino.common.table.Seat;
import com.casino.common.table.Thresholds;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.timing.BetPhaseClockTask;

public class BlackjackDealer implements IDealer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());
	private static final BigDecimal BLACKJACK_FACTOR = new BigDecimal("2.5");
	private final Thresholds thresholds;
	private final BlackjackTable table;
	private List<Card> decks; // 6 decks
	private BlackjackDealerHand dealerHand;
	private ReentrantLock betPhaseLock;

	public BlackjackDealer(BlackjackTable blackjackTable, Thresholds tableConstants) {
		this.table = blackjackTable;
		this.thresholds = tableConstants;
		this.decks = Deck.combineDecks(6);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		betPhaseLock = new ReentrantLock();
	}

	private void startBetPhaseClock() {
		System.out.println("START BET PHASE counter");
		BetPhaseClockTask task = new BetPhaseClockTask(table);
		getTable().startClock(task);
	}

	public void startInsurancePhase() {
		InsurancePhaseClockTask task = new InsurancePhaseClockTask(table);
		getTable().startClock(task);
	}

	@Override
	public boolean hasStartingAce() {
		Card card = dealerHand.getCards().get(0);
		return card != null & card.isAce();
	}

	public void handlePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		System.out.println("Place bet:" + tablePlayer + " bet");
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateStartingBet(bet, table);
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

	public Thresholds getThresholds() {
		return thresholds;
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
		Card card = getCard();
		hand.addCard(card);
	}

	public void handleAdditionalCard(ICasinoPlayer player) {
		Card card = getCard();
		IHand activeHand = player.getActiveHand();
		activeHand.addCard(card);
	}

	public void handleNewPlayer(ICasinoPlayer player) {
		try {
			if (shouldStartBetPhase()) {
				table.setStatus(com.casino.common.table.Status.RUNNING);
				startBetPhaseClock();
			}
		} finally {
			if (betPhaseLock.isHeldByCurrentThread())
				betPhaseLock.unlock();
		}
	}

	private boolean shouldStartBetPhase() {
		return betPhaseLock.tryLock() && table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS && table.getActivePlayerCount() > 0;
	}

	private boolean isAllowedToDealStartingHand() {
		return table.isGamePhase(GamePhase.BETS_COMPLETED) && somebodyHasBet() && isEnoughCardsForPlayersAndDealer();
	}

	private boolean isEnoughCardsForPlayersAndDealer() {
		// Amount of decks used can vary. For example using 100 decks combined.
		int dealer = 1;
		return (table.getPlayers().size() + dealer) * getMaximumNumberOfCardsPlayerCanHold() < decks.size();
	}

	private int getMaximumNumberOfCardsPlayerCanHold() {
		return 12;
	}

	private boolean somebodyHasBet() {
		return getOrderedPlayersWithBet() != null && getOrderedPlayersWithBet().size() > 0;
	}

	private List<ICasinoPlayer> getOrderedPlayersWithBet() {
		return table.getSeats().stream().filter(seat -> seat.hasPlayerWithBet()).sorted(Comparator.comparing(Seat::getNumber)).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public void finalizeBetPhase() {
		handlePlayers();
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
		Seat nextPlayer = table.getNextPlayerWithActiveActiveHand();
		if (nextPlayer != null) {
			table.updatePlayerInTurn(nextPlayer.getPlayer());
			table.updateDealerTurn(false);
		} else {
			table.updateDealerTurn(true);
			table.updatePlayerInTurn(null);
		}
	}

	public void doubleDown(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("cannot doubleDown(), player not found from table:" + player, 0);
		Card cardReference = decks.get(decks.size() - 1);
		player.doubleDown(cardReference);
		getCard();
	}

	public void handleSplit(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("cannot handleSplit(), player not found from table:" + player, 0);
		player.splitStartingHand();
		IHand firstHand = player.getActiveHand();
		firstHand.addCard(getCard());
		if (firstHand.isCompleted()) {
			player.getHands().get(1).activate();
			player.getHands().get(1).addCard(getCard());
		}
	}

	private Card getCard() {
		return decks.remove(decks.size() - 1);
	}

	public void stand(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("cannot stand(), player not found from table:" + player, 0);
		player.stand();
	}

	public void insure(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("cannot insure(), player not found from table:" + player, 0);
		player.insure();
	}

	@Override
	public void completeRound() {
		try {
			if (table.hasPlayersWithWinningChances()) {
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
		List<ICasinoPlayer> playersWithWinningChances = table.getPlayers().stream().filter(player -> player.hasWinningChance()).collect(Collectors.toList());
		playersWithWinningChances.stream().forEach(player -> player.getHands().forEach(playerHand -> {
			int comparison = dealerHand.compareTo(playerHand);
			if (player.isCompensable() && dealerHand.isBlackjack()) {
				player.increaseBalance(player.getFirstHand().getInsuranceBet().multiply(BigDecimal.TWO));
			}
			if (evenResult(comparison))
				player.increaseBalance(playerHand.getBet());
			else if (playerWins(comparison)) {
				if (playerHand.isBlackjack())
					player.increaseBalance(playerHand.getBet().multiply(BLACKJACK_FACTOR));
				else
					player.increaseBalance(playerHand.getBet().multiply(BigDecimal.TWO));
			}
		}));
	}

	private boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private boolean playerWins(int comparison) {
		return comparison > 0;
	}

	private void takeCards() {
		while (!dealerHand.isCompleted()) {
			Card card = getCard();
			LOGGER.fine("Dealer gets card:" + card + " in table:" + table);
			addCard(card);
		}
	}
}
