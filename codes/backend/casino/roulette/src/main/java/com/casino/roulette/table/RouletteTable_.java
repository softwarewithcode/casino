package com.casino.roulette.table;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.game.phase.bet.ParallelBetPhaser;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.Seat;
import com.casino.common.table.structure.SeatedTable;
import com.casino.common.table.structure.TableType;
import com.casino.common.user.User;
import com.casino.roulette.croupier.Croupier;
import com.casino.roulette.croupier.RouletteCroupier;
import com.casino.roulette.export.RouletteTableAPI;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.player.RoulettePlayer;
import com.casino.roulette.player.RoulettePlayer_;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIgnoreProperties(value = { "croupier", "dealer" /* explicitly ignoring croupier */ })
@JsonIncludeProperties(value = { "type", "id", "gamePhase", "wheel", "watcherCount", "seats", "players", "counterTime", "tableCard", "positionChips" })
public final class RouletteTable_ extends SeatedTable<RoulettePlayer> implements RouletteTableAPI, RouletteTable {
	private static final Logger LOGGER = Logger.getLogger(RouletteTable_.class.getName());
	private final RouletteCroupier croupier;
	private final RouletteWheel wheel;

	public RouletteTable_(TableData tableData, RouletteData rouletteData) {
		super(tableData);
		super.tableCard = new TableCard<>(tableData, rouletteData);
		croupier = new Croupier(this, rouletteData);
		wheel = new Wheel(rouletteData);
	}

	@Override
	public GamePhase getGamePhase() {
		return phasePath.getPhase();
	}

	@Override
	public RouletteCroupier getDealer() {
		return croupier;
	}

	@Override
	public boolean join(User user) {
		LOGGER.entering(getClass().getName(), "join", getId());
		try {
			RoulettePlayer player = new RoulettePlayer_(user, this);
			// No real seat required -> null to get any position and chips
			Optional<Seat<RoulettePlayer>> seatOptional = super.join(null, player);
			if (seatOptional.isPresent())
				croupier.onPlayerArrival(player);
			return seatOptional.isPresent();
		} finally {
			LOGGER.exiting(getClass().getName(), "join handled tableId:" + getId());
		}
	}

	@Override
	public void bet(UUID playerId, Integer position, BigDecimal amount) {
		LOGGER.entering(getClass().getName(), "bet");
		try {
			RoulettePlayer player = getPlayer(playerId);
			verifyPlayer(playerId, player);
			croupier.handleChipAddition(player, position, amount);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot accept bet, position= " + position + " table=" + getId() + " player=" + playerId + " amount:" + amount, e);
			throw e;
		} finally {
			LOGGER.exiting(getClass().getName(), "bet");
		}
	}

	@Override
	public void removeBets(UUID playerId, Boolean removeAll) {
		LOGGER.entering(getClass().getName(), "removeBets");
		RoulettePlayer player = null;
		try {
			player = getPlayer(playerId);
			verifyPlayer(playerId, player);
			croupier.handleChipRemoval(player, removeAll);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error lastInList removal  player:" + player, e);
			throw e;
		} finally {
			LOGGER.exiting(getClass().getName(), "removeBets");
		}
	}

	private void verifyPlayer(UUID playerId, CasinoPlayer player) {
		if (player == null)
			throw new IllegalArgumentException("No player with ID=" + playerId + " in RouletteTable:" + getId());
	}

	@Override
	public void removeBetsFromPosition(UUID playerId, Integer position) {
		LOGGER.entering(getClass().getName(), "removeBetsFromPosition");
		RoulettePlayer player = null;
		try {
			player = getPlayer(playerId);
			verifyPlayer(playerId, player);
			croupier.handleChipsRemovalFromPosition(player, position);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in removeBetsFromPosition player:" + player, e);
			throw e;
		} finally {
			LOGGER.exiting(getClass().getName(), "removeBetsFromPosition");
		}
	}

	@Override
	public void repeatLastBets(UUID playerId) {
		LOGGER.entering(getClass().getName(), "repeatLastBets");
		RoulettePlayer player = null;
		try {
			player = getPlayer(playerId);
			verifyPlayer(playerId, player);
			croupier.handlePreviousRoundChipsRepetition(player);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in removeBetsFromPosition player:" + player, e);
			throw e;
		} finally {
			LOGGER.exiting(getClass().getName(), "repeatLastBets");
		}
	}

	@Override
	public void play(UUID playerId, UUID spinId) {
		LOGGER.entering(getClass().getName(), "play " + playerId);
		try {
			if (getType() != TableType.SINGLEPLAYER)
				throw new IllegalPlayerActionException("Play functionality prevented in table " + getId() + " from player " + playerId);
			RoulettePlayer player = getPlayer(playerId);
			verifyPlayer(playerId, player);
			croupier.handleSinglePlayerSpinRequest(player, spinId);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in play functionality, player= " + playerId + " table=" + getId(), e);
			throw e;
		} finally {
			LOGGER.exiting(getClass().getName(), "play");
		}
	}

	@Override
	public RouletteWheel getWheel() {
		return wheel;
	}

	@Override
	public void prepareForNextRound() {
		LOGGER.entering(getClass().getName(), "prepareForNextRound");
		try {
			getLock().lock();
			wheel.prepareNextSpin();
			List<RoulettePlayer> activePlayers = getPlayers().stream().filter(CasinoPlayer::isActive).toList();
			activePlayers.forEach(CasinoPlayer::prepareForNextRound);
			sanitizeSeatsByPlayerStatus(List.of(PlayerStatus.SIT_OUT, PlayerStatus.LEFT));
			if (isMultiplayer())
				updateCounterTime(((ParallelBetPhaser) getDealer()).getBetPhaseTime());
			if (getActivePlayerCount() == 0)
				setStatus(TableStatus.WAITING_PLAYERS);
		} finally {
			getLock().unlock();
			LOGGER.exiting(getClass().getName(), "prepareForNextRound");
		}
	}

}
