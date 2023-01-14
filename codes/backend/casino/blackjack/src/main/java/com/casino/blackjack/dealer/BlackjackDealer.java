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
import com.casino.common.bank.CardGameBank;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.cards.IHand;
import com.casino.common.dealer.CommunicationChannel;
import com.casino.common.dealer.IDealer;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.Seat;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.timing.BetPhaseClockTask;
import com.casino.common.user.Title;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 * 
 */
@JsonIgnoreType
@JsonIncludeProperties(value = { /* explicitly nothing from here */ })
public class BlackjackDealer implements IDealer {
	private static final Logger LOGGER = Logger.getLogger(BlackjackDealer.class.getName());

	private final BlackjackTable table;
	private final ReentrantLock betPhaseLock;
	private final CommunicationChannel voice;
	private List<Card> deck;
	private BlackjackDealerHand dealerHand;

	public BlackjackDealer(BlackjackTable blackjackTable) {
		this.table = blackjackTable;
		this.deck = Deck.pileUpAndShuffle(8);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		this.betPhaseLock = new ReentrantLock();
		this.voice = new CommunicationChannel(table);
	}

	private void startBetPhaseClock(long initialDelay) {
		BetPhaseClockTask task = new BetPhaseClockTask(table);
		getTable().startClock(task, initialDelay);
	}

	public void startInsurancePhase() {
		table.updateGamePhase(GamePhase.INSURE);
		InsurancePhaseClockTask task = new InsurancePhaseClockTask(table);
		getTable().startClock(task, 0);
		notifyAll(Title.INSURANCE_TIME_START, null);
	}

	public boolean hasStartingAce() {
		Card card = dealerHand.getCards().get(0);
		return card != null & card.isAce();
	}

	public void updatePlayerBet(ICasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat> seatStream = table.getSeats().stream();
		Optional<Seat> seatOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		Seat seat = seatOptional.orElseThrow(PlayerNotFoundException::new);
		seat.getPlayer().updateStartingBet(bet, table);
	}

	public BlackjackDealerHand getHand() {
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
		deck = Deck.pileUpAndShuffle(6);
	}

	public List<Card> getDecks() {
		return deck;
	}

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

	public void hit(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		dealCard(player.getActiveHand());
		if (shouldActivateSecondHand(player)) {
			player.activateSecondHand();
			dealCard(player.getActiveHand());
		}
	}

	private boolean shouldActivateSecondHand(BlackjackPlayer player) {
		return player.getHands().get(0).isCompleted() && player.getHands().size() == 2 && !player.getHands().get(1).isActive() && !player.getHands().get(1).isCompleted();
	}

	@Override
	public <T extends CasinoPlayer> void onPlayerArrival(T player) {
		try {
			notifyPlayerArrival(player);
			if (!betPhaseLock.tryLock()) {
				notifyTableStatus(player);
				return;
			}
			if (shouldStartNewGame()) {
				startNewGame();
				notifyAll(Title.BET_TIME_START, (BlackjackPlayer) player);
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

	private boolean shouldStartNewGame() {
		return table.getStatus() == com.casino.common.table.Status.WAITING_PLAYERS || table.getActivePlayerCount() == 1;
	}

	private boolean shouldDealStartingHands() {
		return table.isGamePhase(GamePhase.BETS_COMPLETED) && hasSomebodyBet() && isEnoughCardsForPlayersAndDealer();
	}

	private boolean isEnoughCardsForPlayersAndDealer() {
		// Amount of decks used can vary. For example using 100 decks combined.
		int dealer = 1;
		return (table.getPlayers().size() + dealer) * getMaximumNumberOfCardsPlayerCanHold() < deck.size();
	}

	private static int getMaximumNumberOfCardsPlayerCanHold() {
		return 11; // 11 aces=21 (6 decks contains 24 aces)
	}

	private boolean hasSomebodyBet() {
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
			table.updatePlayersToWatchers(true);
			notifyAll(Title.NO_BETS_NO_DEAL, null);
			return;
		}
		table.updatePlayersToWatchers(false);
		matchPlayersBalancesWithBets();
		dealStartingHands();
		if (hasStartingAce()) {
			startInsurancePhase();
			return;
		}
		table.updateGamePhase(GamePhase.PLAY);
		updateTableActor();
		notifyAll(Title.INITIAL_DEAL_DONE, (BlackjackPlayer) table.getPlayerInTurn());
	}

	private void matchPlayersBalancesWithBets() {
		table.getPlayersWithBet().forEach(player -> matchPlayerBalanceAccordingToBet(player));
	}

	private void matchPlayerBalanceAccordingToBet(ICasinoPlayer player) {
		player.getActiveHand().updateBet(player.getTotalBet());
		player.subtractTotalBetFromBalance();
	}

	private void updatePlayerStatuses() {
		List<ICasinoPlayer> players = table.getSeats().stream().filter(Seat::hasPlayer).map(Seat::getPlayer).toList();
		players.forEach(player -> updatePlayerStatus(player));
	}

	private void updatePlayerStatus(ICasinoPlayer player) {
		if (player.getStatus() == PlayerStatus.LEFT)
			return;
		if (player.hasBet()) {
			player.setStatus(PlayerStatus.ACTIVE);
			player.clearBetRoundSkips();
		} else {
			player.setStatus(PlayerStatus.SIT_OUT);
			player.increaseBetRoundSkips();
		}
	}

	public void updateTableActor() {
		table.stopClock();
		Optional<Seat> optionalPlayerSeat = table.getSeats().stream().filter(seat -> seat.hasPlayerWhoCanAct()).min(Comparator.comparing(Seat::getNumber));
		optionalPlayerSeat.ifPresentOrElse(seat -> table.onPlayerInTurnUpdate(seat.getPlayer()), () -> onDealerTurnChange());
	}

	private void completeRound() {
		try {
			if (table.getGamePhase() != GamePhase.PLAY) {
				LOGGER.severe("complete round called in a wrong phase was: " + table.getGamePhase());
				throw new IllegalStateException("cannot complete round");
			}
			completeActiveHands();
			addDealerCards();
			CardGameBank.matchBalances(table.getPlayersWithBet(), dealerHand);
			table.updateGamePhase(GamePhase.ROUND_COMPLETED);
			if (table.getActivePlayerCount() == 0)
				table.setStatus(com.casino.common.table.Status.WAITING_PLAYERS);
			if (shouldRestartBetPhase()) {
				restartBetPhase();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something unexpected happend. Waiting for brush to arrive.", e);
			BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
			table.onClose();
			throw new IllegalStateException("what to do");
		}
	}

	private void restartBetPhase() {
		table.updateCounterTime(table.getThresholds().phaseDelay().intValue());
		startBetPhaseClock(table.getThresholds().phaseDelay());
	}

	private void sanitizeInactivePlayersSeats() {
		table.findInactivePlayerSeats().forEach(Seat::sanitize);
	}

	public synchronized void prepareNewRound() {
		if (table.getGamePhase() != GamePhase.ROUND_COMPLETED)
			throw new IllegalArgumentException("not allowed");
		table.getPlayers().forEach(ICasinoPlayer::prepareNextRound);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		table.updateGamePhase(GamePhase.BET);
		table.updateCounterTime(table.getThresholds().betPhaseTime());
		deck = Deck.pileUpAndShuffle(8);
		notifyAll(Title.BET_TIME_START, null);
	}

	private boolean shouldRestartBetPhase() {
		return table.getStatus() == com.casino.common.table.Status.RUNNING && table.getActivePlayerCount() > 0 && table.getGamePhase() == GamePhase.ROUND_COMPLETED;
	}

	private void onDealerTurnChange() {
		table.updateDealerTurn(true);
		table.clearPlayerInTurn();
		completeRound();
		notifyAll(Title.ROUND_COMPLETED, null);
	}

	public void doubleDown(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		Card cardReference = deck.get(deck.size() - 1);
		player.doubleDown(cardReference);
		removeLastCardFromDeck();
	}

	public Card getNextCard() {
		return deck.get(deck.size() - 1);
	}

	private void verifyPlayerHasSeat(BlackjackPlayer player) {
		if (!table.hasSeat(player))
			throw new PlayerNotFoundException("Player not found from table:" + player, 0);
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
		if (shouldActivateSecondHand(player)) {
			player.activateSecondHand();
			dealCard(player.getActiveHand());
		}
	}

	public void insure(BlackjackPlayer player) {
		verifyPlayerHasSeat(player);
		player.insure();
	}

	private void completeActiveHands() {
		List<ICasinoPlayer> players = table.getPlayersWithBet().stream().filter(ICasinoPlayer::hasActiveHand).toList();
		players.forEach(player -> player.getActiveHand().complete());
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
		Title title = table.getPlayerInTurn() != null ? Title.PLAYER_TIME_START : Title.ROUND_COMPLETED;
		notifyAll(title, (BlackjackPlayer) table.getPlayerInTurn());
	}

	public void updateActorAndNotify() {
		updateTableActor();
		if (table.getPlayerInTurn() != null)
			notifyAll(Title.PLAYER_TIME_START, (BlackjackPlayer) table.getPlayerInTurn());
	}

	public void onPlayerLeave(BlackjackPlayer leavingPlayer) {
		leavingPlayer.setStatus(PlayerStatus.LEFT);
		if (table.isPlayerInTurn(leavingPlayer)) {
			finalizeInactivePlayerTurn(leavingPlayer);
			updateActorAndNotify();
		}
		if (!table.isRoundRunning()) {
			sanitizeInactivePlayersSeats();
			notifyAll(Title.PLAYER_LEFT, leavingPlayer);
		} else
			notifyAll(Title.SIT_OUT, leavingPlayer);
		if (!table.hasPlayers()) {
			table.stopClock();
			table.setStatus(com.casino.common.table.Status.WAITING_PLAYERS);
		}
	}

	private void finalizeInactivePlayerTurn(BlackjackPlayer player) {
		if (player.hasBet() && table.isPlayerInTurn(player)) {
			autoplayForPlayer(player);
		}
	}

	public void handleTimedoutPlayer(BlackjackPlayer timedOutPlayer) {
		finalizeInactivePlayerTurn(timedOutPlayer);
		notifyAll(Title.TIMED_OUT, timedOutPlayer);
		updateActorAndNotify();
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
