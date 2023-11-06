package com.casino.blackjack.dealer;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.casino.blackjack.bank.BlackjackBank;
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.game.BlackjackData;
import com.casino.blackjack.player.BlackjackHand;
import com.casino.blackjack.player.BlackjackPlayer_;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.util.BlackjackUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.Deck;
import com.casino.common.dealer.PlayerTimingCroupier;
import com.casino.common.dealer.TableGameCroupier;
import com.casino.common.exception.IllegalPhaseException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.game.phase.bet.ParallelBetPhaser;
import com.casino.common.game.phase.insurance.InsurancePhase;
import com.casino.common.game.phase.insurance.InsurancePhaser;
import com.casino.common.message.Event;
import com.casino.common.player.CardPlayer;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.Seat;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIgnoreType
@JsonIncludeProperties(value = { /* explicitly nothing from here */ })
public final class Dealer extends TableGameCroupier implements BlackjackDealer, ParallelBetPhaser, InsurancePhaser, PlayerTimingCroupier {
	private static final Logger LOGGER = Logger.getLogger(Dealer.class.getName());
	private final BlackjackTable table;
	private List<Card> deck;
	private BlackjackDealerHand dealerHand;
	private final BlackjackData blackjackInitData;

	public Dealer(BlackjackTable blackjackTable, BlackjackData gameData) {
		super(blackjackTable);
		this.table = blackjackTable;
		this.deck = Deck.pileUpAndShuffle(8);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		this.blackjackInitData = gameData;
	}

	private void startInsurancePhase() {
		table.updateGamePhase(BlackjackGamePhase.INSURE);
		InsurancePhase<Dealer> task = new InsurancePhase<>(this);
		getTable().startTiming(task, 0);
		voice.notifyEverybody(Event.INSURANCE_TIME_START, null);
	}

	private boolean hasStartingAce() {
		Card card = dealerHand.getCards().get(0);
		return card != null && card.isAce();
	}

	@Override
	public void handleBet(CasinoPlayer tablePlayer, BigDecimal bet) {
		Stream<Seat<BlackjackPlayer_>> seatStream = table.getSeats().stream();
		Optional<Seat<BlackjackPlayer_>> seatOptional = seatStream.filter(seat -> seat.hasPlayer() && seat.getPlayer().equals(tablePlayer)).findFirst();
		Seat<BlackjackPlayer_> seat = seatOptional.orElseThrow(PlayerNotFoundException::new);
		seat.getPlayer().updateStartingBet(bet);
	}

	public BlackjackDealerHand getHand() {// Public for tests?!
		return dealerHand;
	}

	public List<Card> getDecks() { // Public for tests?!
		return deck;
	}

	@Override
	public BlackjackTable getTable() {
		return table;
	}

	public void dealStartingHands() {
		LOGGER.fine("BlackjackDealer deals starting hands in table:" + getTable());
		List<BlackjackPlayer_> orderedPlayers = table.getOrderedPlayersWithBet();
		orderedPlayers.forEach(player -> dealCard(deck, player.getActiveHand())); // first the players
		dealCard(deck, dealerHand); // then the dealer
		orderedPlayers.forEach(player -> dealCard(deck, player.getActiveHand())); // then the players again
	}

	@Override
	public void handleHit(BlackjackPlayer_ player) {
		table.verifyPlayerHasSeat(player);
		dealCard(deck, player.getActiveHand());
		if (shouldActivateSecondHand(player)) {
			player.activateSecondHand();
			dealCard(deck, player.getActiveHand());
		}
		updateActorAndNotify();
	}

	private boolean shouldActivateSecondHand(BlackjackPlayer_ player) {
		return player.getHands().get(0).isCompleted() && player.getHands().size() == 2 && !player.getHands().get(1).isActive() && !player.getHands().get(1).isCompleted();
	}

	@Override
	protected void startGame() {
		// Game, GameStatus? tableStatus??
		table.setStatus(TableStatus.RUNNING);
		startBetPhase();
	}

	private void startBetPhase() {
		table.updateGamePhase(BlackjackGamePhase.BET);
		table.getPlayers().forEach(CasinoPlayer::updateAvailableActions);
		dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		startParallelBetPhase(0L);
		voice.notifyEverybody(Event.BET_TIME_START, null);
	}

	private boolean shouldDealStartingHands() {
		return table.isGamePhase(BlackjackGamePhase.BETS_COMPLETED) && hasSomebodyBet() && isEnoughCardsForPlayersAndDealer();
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
		List<BlackjackPlayer_> c = table.getOrderedPlayersWithBet();
		return c != null && c.size() > 0;
	}

	private synchronized void finalizeBetPhase() {
		table.updateGamePhase(BlackjackGamePhase.BETS_COMPLETED);
		updatePlayerStatuses();
		if (!shouldDealStartingHands()) {
			LOGGER.fine("dealer does not deal cards now");
			// table.updatePlayersToWatchers(true);
			voice.notifyEverybody(Event.NO_BETS_NO_DEAL, null);
			return;
		}
		matchPlayersBalancesWithBets();
		dealStartingHands();
		if (hasStartingAce()) {
			startInsurancePhase();
			return;
		}
		table.updateGamePhase(BlackjackGamePhase.PLAY);
		updateActorAndClock();
		voice.notifyEverybody(Event.INITIAL_DEAL_DONE, table.getActivePlayer());
	}

	private void matchPlayersBalancesWithBets() {
		table.getPlayersWithBet().forEach(this::matchPlayerBalanceAccordingToBet);
	}

	private void matchPlayerBalanceAccordingToBet(BlackjackPlayer_ player) {
		BlackjackHand hand = player.getActiveHand();
		hand.updateBet(player.getTotalBet());
		player.subtractTotalBetFromBalance();
	}

	private void updatePlayerStatuses() {
		List<BlackjackPlayer_> players = table.getSeats().stream().filter(Seat::hasPlayer).map(Seat::getPlayer).toList();
		players.forEach(this::updatePlayerStatus);
	}

	private void updatePlayerStatus(CasinoPlayer player) {
		if (player.getStatus() == PlayerStatus.LEFT)
			return;
		if (player.hasBet()) {
			player.setStatus(PlayerStatus.ACTIVE);
			player.clearSkips();
		} else {
			player.setStatus(PlayerStatus.SIT_OUT_NEXT_HAND);
			player.increaseSkips();
		}
	}

	private void updateActorAndClock() {
		table.stopTiming();
		Optional<Seat<BlackjackPlayer_>> optionalPlayerSeat = table.getSeats()
				.stream()
				.filter(seat -> seat.hasActivePlayer())
				.filter(seat -> seat.hasPlayerWithBet())
				.filter(seat -> seat.getPlayer().hasActiveHand())
				.min(Comparator.comparing(Seat::getNumber));
		optionalPlayerSeat.ifPresentOrElse(seat -> table.onActivePlayerChange(seat.getPlayer()), this::onDealerTurnChange);
	}

	private void completeRound() {
		try {
			if (table.getGamePhase() != BlackjackGamePhase.PLAY) {
				LOGGER.severe("complete round called in a wrong phase was: " + table.getGamePhase());
				throw new IllegalStateException("cannot complete round");
			}
			completeActiveHands();
			addDealerCards();
			BlackjackBank.matchBalances(table.getPlayersWithBet(), dealerHand);
			table.updateGamePhase(BlackjackGamePhase.ROUND_COMPLETED);
			if (table.getActivePlayerCount() == 0)
				table.setStatus(TableStatus.WAITING_PLAYERS);
			if (shouldRestartBetPhase())
				restartBetPhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something unexpected happened. Waiting for brush to arrive.", e);
			BlackjackUtil.dumpTable(table, "dealer player turn:" + e);
			table.onClose();
			throw new IllegalStateException("what to do");
		}
	}

	private void restartBetPhase() {
		table.updateCounterTime(this.blackjackInitData.getRoundDelay().intValue());
		startParallelBetPhase(this.blackjackInitData.getRoundDelay());
	}

	private void sanitizeInactiveSeats() {
		table.findInactivePlayerSeats().forEach(Seat::sanitize);
	}

	@Override
	public synchronized void reInitializeBetPhase() {
		if (table.getGamePhase() != BlackjackGamePhase.ROUND_COMPLETED)
			throw new IllegalArgumentException("not allowed");
		table.getPlayers().forEach(CasinoPlayer::prepareForNextRound);
		this.dealerHand = new BlackjackDealerHand(UUID.randomUUID(), true);
		table.updateGamePhase(BlackjackGamePhase.BET);
		table.updateCounterTime(getBetPhaseTime());
		deck = Deck.pileUpAndShuffle(8);
		voice.notifyEverybody(Event.BET_TIME_START, null);
	}

	@Override
	public Integer getCounterTime() {
		return ParallelBetPhaser.super.getCounterTime();
	}

	@Override
	public boolean shouldRestartBetPhase() {
		return table.getStatus() == TableStatus.RUNNING && table.getActivePlayerCount() > 0 && table.getGamePhase() == BlackjackGamePhase.ROUND_COMPLETED;
	}

	private void onDealerTurnChange() {
		table.updateDealerTurn(true);
		table.clearActivePlayer();
		completeRound();
		voice.notifyEverybody(Event.ROUND_COMPLETED, null);
	}

	@Override
	public void handleDoubleDown(BlackjackPlayer_ player) {
		table.verifyPlayerHasSeat(player);
		Card cardReference = deck.get(deck.size() - 1); // TODO could deal card here without lock? Balance check is done
		// in doubleDown method
		player.doubleDown(cardReference);
		removeLastCardFromDeck(deck);
		updateActorAndNotify();
	}

	private Card getNextCard() {
		return deck.get(deck.size() - 1);
	}

	@Override
	public void handleSplit(BlackjackPlayer_ player) {
		table.verifyPlayerHasSeat(player);
		player.splitStartingHand();
		BlackjackHand firstHand = player.getActiveHand();
		dealCard(deck, player.getFirstHand());
		if (firstHand.isCompleted()) {
			player.getHands().get(1).activate();
			dealCard(deck, player.getHands().get(1));
		}
		updateActorAndNotify();
	}

	private void autoplayForPlayer(BlackjackPlayer_ player) {
		Card nextCard = getNextCard();
		if (player.autoplay(nextCard).isEmpty())
			removeLastCardFromDeck(deck);
	}

	@Override
	public void handleStand(BlackjackPlayer_ player) {
		table.verifyPlayerHasSeat(player);
		player.stand();
		if (shouldActivateSecondHand(player)) {
			player.activateSecondHand();
			dealCard(deck, player.getActiveHand());
		}
		updateActorAndNotify();
	}

	@Override
	public void handleInsure(BlackjackPlayer_ player) {
		table.verifyPlayerHasSeat(player);
		player.insure();
	}

	private void completeActiveHands() {
		List<BlackjackPlayer_> players = table.getPlayersWithBet().stream().filter(CardPlayer::hasActiveHand).toList();
		players.forEach(player -> player.getActiveHand().complete());
	}

	private void addDealerCards() {
		while (!dealerHand.isCompleted()) {
			LOGGER.fine("Dealer takes cards:");
			dealCard(deck, dealerHand);
		}
	}

	private void finalizeInsurancePhase() {
		table.updateGamePhase(BlackjackGamePhase.PLAY);
		updateActorAndClock();
		Event title = table.getActivePlayer() != null ? Event.PLAYER_TIME_START : Event.ROUND_COMPLETED;
		voice.notifyEverybody(title, table.getActivePlayer());
	}

	private void updateActorAndNotify() {
		updateActorAndClock();
		if (table.getActivePlayer() != null)
			voice.notifyEverybody(Event.PLAYER_TIME_START, table.getActivePlayer());
	}

	private void finalizePlayerTurn(BlackjackPlayer_ player) {
		if (player.hasBet() && table.isActivePlayer(player))
			autoplayForPlayer(player);
	}

	private void handleTimedOutPlayer(BlackjackPlayer_ timedOutPlayer) {
		finalizePlayerTurn(timedOutPlayer);
		voice.notifyEverybody(Event.TIMED_OUT, timedOutPlayer);
		updateActorAndNotify();
	}

	@Override
	public <T extends CasinoPlayer> void onPlayerLeave(T leavingPlayer) {
		leavingPlayer.setStatus(PlayerStatus.LEFT);
		if (table.isActivePlayer(leavingPlayer)) {
			finalizePlayerTurn((BlackjackPlayer_) leavingPlayer);
			updateActorAndNotify();

		}
		if (!table.isRoundRunning()) {
			sanitizeInactiveSeats();
			voice.notifyEverybody(Event.PLAYER_LEFT, leavingPlayer);
		} else
			voice.notifyEverybody(Event.SIT_OUT, leavingPlayer);
		if (!table.hasPlayers()) {
			table.stopTiming();
			table.setStatus(TableStatus.WAITING_PLAYERS);
		}
	}

	@Override
	public void onPlayerTimeout(CasinoPlayer timedOutPlayer) {
		LOGGER.entering(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer);
		try {
			table.lock();
			if (!table.isActivePlayer(timedOutPlayer))
				return;
			handleTimedOutPlayer((BlackjackPlayer_) timedOutPlayer);
		} finally {
			table.unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "onPlayerTimeout", " timedOutPlayer:" + timedOutPlayer);
		}
	}

	@Override
	public BlackjackData getGameData() {
		return this.blackjackInitData;
	}

	@Override
	public void onInsurancePhaseEnd() {
		LOGGER.entering(getClass().getName(), "onInsurancePhaseEnd" + getTable());
		try {
			table.lock();
			if (!table.isGamePhase(BlackjackGamePhase.INSURE))
				throw new IllegalPhaseException("GamePhase is not what is expected on insurancePhaseEnd", table.getGamePhase().toString(), BlackjackGamePhase.INSURE.toString());
			finalizeInsurancePhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onInsurancePhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(getTable(), "onBetPhaseEnd");
			table.updateGamePhase(BlackjackGamePhase.ERROR);
		} finally {
			table.getLock().unlock();
			LOGGER.exiting(getClass().getName(), "onInsurancePhaseEnd" + getTable());
		}
	}

	@Override
	public Integer getInsurancePhaseTime() {
		return this.blackjackInitData.insurancePhaseTime();
	}

	@Override
	public void onBetPhaseEnd() {
		try {
			table.lock();
			if (!table.isGamePhase(BlackjackGamePhase.BET))
				throw new IllegalPhaseException("GamePhase is not what is expected on betPhaseEnd", table.getGamePhase().toString(), BlackjackGamePhase.BET.toString());
			finalizeBetPhase();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "onBetPhaseEnd() something went wrong. Waiting for manager's call.", e);
			BlackjackUtil.dumpTable(getTable(), "onBetPhaseEnd");
			table.updateGamePhase(BlackjackGamePhase.ERROR);
		} finally {
			table.unlockPlayerInTurn();
			LOGGER.exiting(getClass().getName(), "onBetPhaseEnd" + this.getTableId());
		}
	}

	@Override
	public Integer getBetPhaseTime() {
		return this.blackjackInitData.betPhaseTime();
	}

	@Override
	public UUID getTableId() {
		return getTable().getId();
	}

	@Override
	public Integer getPlayerTurnTime() {
		return this.blackjackInitData.playerTime();
	}

	@JsonProperty // Getter for serialization
	public BlackjackDealerHand getDealerHand() {
		return dealerHand;
	}
}
