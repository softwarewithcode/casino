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

/**
 * @author softwarewithcode from GitHub
 * 
 */
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
		notifyAll(Title.INSURANCE_PHASE_STARTS, null);
	}

	public boolean dealerHasStartingAce() {
		Card card = dealerHand.getCards().get(0);
		return card != null & card.isAce();
	}

	public void updatePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> seatOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		Seat seat = seatOptional.orElseThrow(PlayerNotFoundException::new);
		seat.getPlayer().updateStartingBet(bet, table);
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

	public void dealStartingHands() {
		LOGGER.info("dealer deals starting hands");
		List<ICasinoPlayer> orderedPlayers = getOrderedPlayersWithBet();
		orderedPlayers.forEach(player -> dealCard(player.getActiveHand())); // first the players
		dealCard(dealerHand); // then the dealer
		orderedPlayers.forEach(player -> dealCard(player.getActiveHand())); // then the players again
	}

	private void dealCard(IHand hand) {
		Card card = removeLastCardFromDeck();
		hand.addCard(card);
	}

	public void addPlayerCard(ICasinoPlayer player) {
		dealCard(player.getActiveHand());
	}

	@Override
	public <T extends CasinoPlayer> void onPlayerArrival(T player) {
		try {
			notifyPlayerArrival(player);
			if (!betPhaseLock.tryLock()) {
				notifyTableStatus(player);
				return;
			}
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
		table.updateGamePhase(GamePhase.BET);
		dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		startBetPhaseClock(0l);
	}

	private void notifyTableStatus(CasinoPlayer player) {
		String message = Mapper.createMessage(Title.STATUS_UPDATE, table, player);
		voice.unicast(message, player);
	}

	private void notifyPlayerArrival(CasinoPlayer player) {
		String commonMessage = Mapper.createMessage(Title.NEW_PLAYER, table, player);
		// no need to serialize again, -> string manipulation
		String loginMessage = Mapper.createMessage(Title.LOGIN, table, player);
		voice.unicast(loginMessage, player);
		voice.multicast(commonMessage, player);
	}

	public void notifyAll(Title title, BlackjackPlayer player) {
		String message = Mapper.createMessage(title, table, player);
		voice.broadcast(message);
	}

	private boolean shouldStartGame() {
		return table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS || table.getActivePlayerCount() == 1;
	}

	private boolean shouldDealStartingHands() {
		System.out.println("ShouldStartDealing?:" + table.isGamePhase(GamePhase.BETS_COMPLETED) + "  bet:" + somebodyHasBet());
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
		// table.getSeats().stream().filter(Seat::hasPlayerWithBet).findAny
		List<ICasinoPlayer> c = getOrderedPlayersWithBet();
		return c != null && c.size() > 0;
	}

	private List<ICasinoPlayer> getOrderedPlayersWithBet() {
		return table.getSeats().stream().filter(Seat::hasPlayerWithBet).sorted(Comparator.comparing(Seat::getNumber)).map(seat -> seat.getPlayer()).collect(Collectors.toList());
	}

	public synchronized void finalizeBetPhase() {
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
		updatePlayerStatuses();
		if (!shouldDealStartingHands()) {
			LOGGER.info("dealer does not deal cards now");
			// removeInactivePlayers(); commented for testing
			return;
		}
		subtractBetFromBalance();
		dealStartingHands();
		if (dealerHasStartingAce()) {
			startInsurancePhase();
			return;
		}
		table.updateGamePhase(GamePhase.PLAY);
		updateTableActor();
		notifyAll(Title.INITIAL_DEAL_DONE, (BlackjackPlayer) table.getPlayerInTurn());
	}

	private void subtractBetFromBalance() {
		table.getPlayersWithBet().forEach(player -> {
			player.subtractTotalBetFromBalance();
			player.getHands().get(0).updateBet(player.getTotalBet());
		});
	}

	private void updatePlayerStatuses() {
		List<ICasinoPlayer> players = table.getSeats().stream().filter(Seat::hasPlayer).map(Seat::getPlayer).collect(Collectors.toList());
		players.forEach(player -> {
			if (player.getStatus() == Status.LEFT)
				return;
			if (player.hasBet())
				player.setStatus(Status.ACTIVE);
			else
				player.setStatus(Status.SIT_OUT);
		});
	}

	public void updateTableActor() {
		table.stopClock();
		Optional<Seat> optionalPlayerActor = table.getSeats().stream().filter(seat -> seat.hasPlayerWhoCanAct()).min(Comparator.comparing(Seat::getNumber));
		if (optionalPlayerActor.isEmpty()) {
			changeTurnToDealer();
			carryOutDealerDutiesAndNotify();
		} else {
			// Actor can be same as previous ->split hand
			BlackjackPlayer player = (BlackjackPlayer) optionalPlayerActor.get().getPlayer();
			table.changePlayer(player);
			player.updateActions();
		}
	}

	public void informTable() {
		notifyAll(Title.SERVER_WAITS_PLAYER_ACTION, (BlackjackPlayer) table.getPlayerInTurn());
	}

	private void carryOutDealerDutiesAndNotify() {
		completeRound();
		notifyAll(Title.ROUND_COMPLETED, null);
		removeInactivePlayers();
		if (table.getActivePlayerCount() == 0)
			table.setStatus(com.casino.common.table.Status.WAITING_PLAYERS);
		if (shouldRestartBetPhase()) {
			startBetPhaseClock(table.getThresholds().phaseDelay());
		}
	}

	private void removeInactivePlayers() {
		List<Seat> casinoPlayers = table.getSeats().stream().filter(seat -> seat.hasPlayer() && seat.getPlayer().getStatus() != Status.ACTIVE).collect(Collectors.toList());
		casinoPlayers.forEach(Seat::sanitize);
	}

	public synchronized void prepareNewRound() {
		if (table.getGamePhase() != GamePhase.ROUND_COMPLETED)
			throw new IllegalArgumentException("not allowed");
		table.getPlayers().forEach(ICasinoPlayer::prepareNextRound);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		table.updateGamePhase(GamePhase.BET);
		table.updateCounterTime(table.getThresholds().betPhaseTime());
		deck = Deck.combineDecks(8);
		notifyAll(Title.BET_PHASE_STARTS, null);
	}

	private boolean shouldRestartBetPhase() {
		return table.getStatus() == com.casino.common.table.Status.RUNNING && table.getActivePlayerCount() > 0;
	}

	private void changeTurnToDealer() {
		table.updateDealerTurn(true);
		table.clearPlayerInTurn();
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
		LOGGER.info("Dealer starts completeRound");
		if (table.getGamePhase() == GamePhase.ROUND_COMPLETED) {
			LOGGER.severe("complete round called on completed round");
			return;
		}
		try {
			completeActiveHands();
			if (table.hasPlayersWithWinningChances()) {
				addDealerCards();
				handlePayouts();
			}
			table.updateGamePhase(GamePhase.ROUND_COMPLETED);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something unexpected happend. Waiting for brush to arrive.", e);
			BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
			throw new IllegalStateException("what to do");
		}
	}

	private void completeActiveHands() {
		List<ICasinoPlayer> players = table.getPlayersWithBet().stream().filter(ICasinoPlayer::hasActiveHand).toList();
		players.forEach(player -> player.getActiveHand().complete());
	}

	private void handlePayouts() {
		LOGGER.info("Dealer starts payout");
		// Deal the case where player has disconnected before payout
		List<ICasinoPlayer> playersWithWinningChances = table.getPlayersWithBet().stream().filter(ICasinoPlayer::hasWinningChance).toList();
		playersWithWinningChances.forEach(player -> payoutWinnings(player));
	}

	private void payoutWinnings(ICasinoPlayer player) {
		player.getHands().forEach(playerHand -> {
			if (shouldPayInsuranceBet(playerHand))
				player.increaseBalanceAndPayout(playerHand.getInsuranceBet().multiply(BigDecimal.TWO));
			BigDecimal betMultiplier = determineBetMultiplier(playerHand);
			BigDecimal handPayout = playerHand.getBet().multiply(betMultiplier);
			player.increaseBalanceAndPayout(handPayout);
		});
	}

	private boolean shouldPayInsuranceBet(IHand playerHand) {
		return playerHand.isInsuranceCompensable() && dealerHand.isBlackjack();
	}

	private BigDecimal determineBetMultiplier(IHand playerHand) {
		int handComparison = dealerHand.compareTo(playerHand);
		if (evenResult(handComparison))
			return BigDecimal.ONE;
		if (playerWins(handComparison))
			return playerHand.isBlackjack() ? BLACKJACK_FACTOR : BigDecimal.TWO;
		return BigDecimal.ZERO;
	}

	private boolean evenResult(int comparison) {
		return comparison == 0;
	}

	private boolean playerWins(int comparison) {
		return comparison > 0;
	}

	private void addDealerCards() {
		while (!dealerHand.isCompleted()) {
			LOGGER.info("Dealer takes cards:");
			Card card = removeLastCardFromDeck();
			addCard(card);
		}
	}

	public void finalizeInsurancePhase() {
		table.updateGamePhase(GamePhase.PLAY);
		updateTableActor();
		Title title = table.getPlayerInTurn() != null ? Title.SERVER_WAITS_PLAYER_ACTION : Title.ROUND_COMPLETED;
		notifyAll(title, (BlackjackPlayer) table.getPlayerInTurn());
	}

	public void calculateNextActorTurnAndNotify() {
		updateTableActor();
		if (table.getPlayerInTurn() != null)
			notifyAll(Title.SERVER_WAITS_PLAYER_ACTION, (BlackjackPlayer) table.getPlayerInTurn());
	}

	public void handleLeavingPlayer(BlackjackPlayer leavingPlayer) {
		leavingPlayer.setStatus(Status.LEFT);
		finishInactivePlayerTurn(leavingPlayer);
		if (!table.getGamePhase().isOnGoingRound()) {
			removeInactivePlayers();
			notifyAll(Title.PLAYER_LEFT, leavingPlayer);
		} else
			notifyAll(Title.SIT_OUT, leavingPlayer);
		if (table.getPlayers().size() == 0) {
			table.stopClock();
			table.setStatus(com.casino.common.table.Status.WAITING_PLAYERS);
		}
	}

	private void finishInactivePlayerTurn(BlackjackPlayer player) {
		if (player.hasBet() && player.equals(table.getPlayerInTurn())) {
			autoplayForPlayer(player);
			calculateNextActorTurnAndNotify();
		}
	}

	public void handleTimedoutPlayer(BlackjackPlayer timedOutPlayer) {
		finishInactivePlayerTurn(timedOutPlayer);
		notifyAll(Title.TIMED_OUT, timedOutPlayer);
		calculateNextActorTurnAndNotify();
	}

	public void sendStatusUpdate(CasinoPlayer player) {
		String statusMessage = Mapper.createMessage(Title.STATUS_UPDATE, table, player);
		voice.unicast(statusMessage, player);
	}

	@Override
	public <T extends CasinoPlayer> void onWatcherArrival(T watcher) {
		String message = Mapper.createMessage(Title.OPEN_TABLE, table, null);
		voice.unicast(message, watcher);
	}
}
