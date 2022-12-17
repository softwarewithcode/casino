package com.casino.blackjack.dealer;

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

import com.casino.blackjack.message.Mapper;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.table.BlackjackUtil;
import com.casino.blackjack.table.timing.InsurancePhaseClockTask;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.dealer.CommunicationChannel;
import com.casino.common.dealer.IDealer;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.Status;
import com.casino.common.table.Seat;
import com.casino.common.table.Thresholds;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.timing.BetPhaseClockTask;
import com.casino.common.user.Title;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BlackjackDealer implements IDealer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());
	private static final BigDecimal BLACKJACK_FACTOR = new BigDecimal("2.5");
	@JsonIgnore
	private final Thresholds thresholds;
	@JsonIgnore
	private final BlackjackTable table;
	@JsonIgnore
	private final ReentrantLock betPhaseLock;
	@JsonIgnore
	private final CommunicationChannel voice;
	@JsonIgnore
	private List<Card> deck;
	@JsonIgnore
	private BlackjackDealerHand dealerHand;

	public BlackjackDealer(BlackjackTable blackjackTable, Thresholds thresholds) {
		this.table = blackjackTable;
		this.thresholds = thresholds;
		this.deck = Deck.combineDecks(8);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		betPhaseLock = new ReentrantLock();
		voice = new CommunicationChannel(table);
	}

	private void startBetPhaseClock(long initialDelay) {
		BetPhaseClockTask task = new BetPhaseClockTask(table);
		getTable().startClock(task, initialDelay);
	}

	public void startInsurancePhase() {
		table.updateGamePhase(GamePhase.INSURE);
		InsurancePhaseClockTask task = new InsurancePhaseClockTask(table);
		getTable().startClock(task, 0);
	}

	@Override
	public boolean hasStartingAce() {
		Card card = dealerHand.getCards().get(0);
		return card != null & card.isAce();
	}

	public void updatePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> playerOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		playerOptional.ifPresentOrElse(seat -> {
			seat.getPlayer().updateStartingBet(bet, table);
		}, () -> {
			throw new PlayerNotFoundException("Player not found in table:" + table + " player:" + tablePlayer, 1);
		});
	}

	@JsonIgnore
	public IHand getHand() {
		return dealerHand;
	}

	public boolean isRoundCompleted() {
		return table.getGamePhase() == GamePhase.ROUND_COMPLETED;
	}

	public void addCard(Card card) {
		this.dealerHand.addCard(card);
	}

	public void initTable() {
		createDecks();
	}

	private void createDecks() {
		deck = Deck.combineDecks(6);
	}

	// @JsonIgnore just in case of an upcoming getter error
	@JsonIgnore
	public List<Card> getDecks() {
		return deck;
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	@JsonIgnore
	public BlackjackTable getTable() {
		return table;
	}

	public boolean dealStartingHands() {
		LOGGER.info("dealer deals starting hands");
		List<ICasinoPlayer> orderedPlayers = getOrderedPlayersWithBet();
		orderedPlayers.forEach(player -> dealCard(player.getHands().get(0))); // first the players
		dealCard(dealerHand); // then dealer
		orderedPlayers.forEach(player -> { // then players again
			dealCard(player.getHands().get(0));
		});
		return true;
	}

	private void dealCard(IHand hand) {
		Card card = removeLastCardFromDeck();
		hand.addCard(card);
	}

	public void addPlayerCard(ICasinoPlayer player) {
		Card card = removeLastCardFromDeck();
		IHand activeHand = player.getActiveHand();
		activeHand.addCard(card);
	}

	@Override
	public <T extends CasinoPlayer> void onPlayerArrival(T player) {
		try {
			notifyPlayerArrival((BlackjackPlayer) player);
			if (!betPhaseLock.tryLock())
				return;
			if (shouldStartGame()) {
				startNewGame();
				notifyAll(Title.BET_PHASE_STARTS, (BlackjackPlayer) player);
			}
		} finally {
			if (betPhaseLock.isHeldByCurrentThread())
				betPhaseLock.unlock();
		}
	}

	private void startNewGame() {
		table.setStatus(com.casino.common.table.Status.RUNNING);
		startBetPhaseClock(0l);
	}

	private void notifyPlayerArrival(BlackjackPlayer player) {
		String commonMessage = Mapper.createMessage(Title.NEW_PLAYER, table, player);
		String loginMessage = Mapper.createMessage(Title.LOGIN, table, player);
		voice.unicast(loginMessage, player);
		voice.multicast(commonMessage, player);
	}

	public void notifyAll(Title title, BlackjackPlayer player) {
		String message = Mapper.createMessage(title, table, player);
		voice.broadcast(message);
	}

	private boolean shouldStartGame() {
		return table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS && table.getActivePlayerCount() > 0;
	}

	private boolean shouldDealStartingHands() {
		return table.isGamePhase(GamePhase.BETS_COMPLETED) && somebodyHasBet() && isEnoughCardsForPlayersAndDealer();
	}

	private boolean isEnoughCardsForPlayersAndDealer() {
		// Amount of decks used can vary. For example using 100 decks combined.
		int dealer = 1;
		return (table.getPlayers().size() + dealer) * getMaximumNumberOfCardsPlayerCanHold() < deck.size();
	}

	private int getMaximumNumberOfCardsPlayerCanHold() {
		return 11; // 11 aces=21 (6 decks contains 24 aces)
	}

	private boolean somebodyHasBet() {
		List<ICasinoPlayer> c = getOrderedPlayersWithBet();
		return c != null && c.size() > 0;
	}

	private List<ICasinoPlayer> getOrderedPlayersWithBet() {
		return table.getSeats().stream().filter(Seat::hasPlayerWithBet).sorted(Comparator.comparing(Seat::getNumber)).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public synchronized void finalizeBetPhase() {
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
		if (!shouldDealStartingHands()) {
			LOGGER.info("dealer does not deal cards now");
			return;
		}
		updateActivePlayers();
		dealStartingHands();
		if (hasStartingAce()) {
			notifyAll(Title.INSURANCE_PHASE_STARTS, (BlackjackPlayer) table.getPlayerInTurn());
			startInsurancePhase();
			return;
		}
		table.updateGamePhase(GamePhase.PLAY);
		updateTableActor();
		notifyAll(Title.PLAY_TURN, (BlackjackPlayer) table.getPlayerInTurn());
	}

	private void updateActivePlayers() {
		table.getSeats().stream().filter(Seat::hasPlayer).map(Seat::getPlayer).forEach(player -> {
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

	public void updateTableActor() {
		Optional<Seat> optionalPlayerActor = table.getSeats().stream().filter(seat -> seat.hasPlayerWithBet() && seat.getPlayer().hasActiveHand()).min(Comparator.comparing(Seat::getNumber));
		table.stopClock();
		if (optionalPlayerActor.isEmpty()) {
			changeTurnToDealer();
			carryOutDealerTurn();
		} else {
			// Actor can be same as previous ->split hand
			BlackjackPlayer player = (BlackjackPlayer) optionalPlayerActor.get().getPlayer();
			table.changePlayer(player);
			player.updateActions();
		}
	}

	public void informTable() {
		notifyAll(Title.PLAY_TURN, (BlackjackPlayer) table.getPlayerInTurn());
	}

	private void carryOutDealerTurn() {
		completeRound();
		if (shouldRestartBetPhase()) {
			startBetPhaseClock(table.getThresholds().phaseDelay());
		}
	}

	public synchronized void prepareNewRound() {
		if (table.getGamePhase() != GamePhase.ROUND_COMPLETED)
			throw new IllegalArgumentException("not allowed");
		table.getPlayers().values().forEach(ICasinoPlayer::prepareNextRound);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		table.updateGamePhase(GamePhase.BET);
		deck = Deck.combineDecks(8);
	}

	private boolean shouldRestartBetPhase() {
		return table.getStatus() == com.casino.common.table.Status.RUNNING && table.getActivePlayerCount() > 0;
	}

	private void changeTurnToDealer() {
		table.updateDealerTurn(true);
		table.changePlayer(null);
	}

	public void doubleDown(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		Card cardReference = deck.get(deck.size() - 1);
		player.doubleDown(cardReference);
		removeLastCardFromDeck();
	}

	@JsonIgnore
	public Card getNextCard() {
		return deck.get(deck.size() - 1);
	}

	private void verifyPlayerHasSeat(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("cannot doubleDown(), player not found from table:" + player, 0);
	}

	public void handleSplit(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		player.splitStartingHand();
		IHand firstHand = player.getActiveHand();
		firstHand.addCard(removeLastCardFromDeck());
		if (firstHand.isCompleted()) {
			player.getHands().get(1).activate();
			player.getHands().get(1).addCard(removeLastCardFromDeck());
		}
	}

	public void autoplayForPlayer(ICasinoPlayer player) {
		Card nextCard = getNextCard();
		if (player.autoplay(nextCard).isEmpty())
			removeLastCardFromDeck();
	}

	private Card removeLastCardFromDeck() {
		Card c = deck.remove(deck.size() - 1);
		return c;
	}

	public boolean shouldChangeTurn() {
		return table.getPlayerInTurn() != null && !table.getPlayerInTurn().hasActiveHand();
	}

	public void stand(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		player.stand();
	}

	public void insure(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		player.insure();
	}

	private void completeRound() {
		if (table.getGamePhase() == GamePhase.ROUND_COMPLETED) {
			LOGGER.severe("complete round called on completed round");
			return;
		}
		try {
			if (table.hasPlayersWithWinningChances()) {
				addDealerCards();
				payout();
			}
			table.updateGamePhase(GamePhase.ROUND_COMPLETED);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something unexpected happend. Waiting for brush to arrive.", e);
			BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
			throw new IllegalStateException("what to do");
		}
	}

	private void payout() {
		// Deal the case where player has disconnected before payout
		List<ICasinoPlayer> playersWithWinningChances = table.getPlayers().values().stream().filter(ICasinoPlayer::hasWinningChance).collect(Collectors.toList());
		playersWithWinningChances.stream().forEach(player -> player.getHands().forEach(playerHand -> {
			int comparison = dealerHand.compareTo(playerHand);
			if (player.isCompensable() && dealerHand.isBlackjack())
				player.increaseBalanceAndPayout(player.getInsuranceAmount().multiply(BigDecimal.TWO));
			if (evenResult(comparison))
				player.increaseBalanceAndPayout(playerHand.getBet());
			else if (playerWins(comparison)) {
				if (playerHand.isBlackjack())
					player.increaseBalanceAndPayout(playerHand.getBet().multiply(BLACKJACK_FACTOR));
				else
					player.increaseBalanceAndPayout(playerHand.getBet().multiply(BigDecimal.TWO));
			}
		}));
	}

	private boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private boolean playerWins(int comparison) {
		return comparison > 0;
	}

	private void addDealerCards() {
		while (!dealerHand.isCompleted()) {
			Card card = removeLastCardFromDeck();
			addCard(card);
		}
	}

	public void finalizeInsurancePhase() {
		table.updateGamePhase(GamePhase.PLAY);
		updateTableActor();
		Title title = table.getPlayerInTurn() != null ? Title.PLAY_TURN : Title.ROUND_COMPLETED;
		notifyAll(title, (BlackjackPlayer) table.getPlayerInTurn());
	}

	public void finalizeAction(BlackjackPlayer player) {
		updateTableActor();
		if (table.getPlayerInTurn() != null)
			notifyAll(Title.PLAY_TURN, (BlackjackPlayer) table.getPlayerInTurn());
		else
			notifyAll(Title.ROUND_COMPLETED, (BlackjackPlayer) table.getPlayerInTurn());
	}
}
