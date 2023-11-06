package com.casino.roulette.player;

import com.casino.common.action.PlayerAction;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.functions.Functions;
import com.casino.common.player.Player;
import com.casino.common.player.PlayerStatus;
import com.casino.common.ranges.Range;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.user.User;
import com.casino.common.validation.Verifier;
import com.casino.roulette.bet.Bet;
import com.casino.roulette.bet.BetData;
import com.casino.roulette.bet.RouletteBet;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.casino.roulette.export.RoulettePlayerAction;
import com.casino.roulette.game.RouletteGamePhase;
import com.casino.roulette.persistence.PlayerResult;
import com.casino.roulette.persistence.RoundResult;
import com.casino.roulette.persistence.SpinResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties(value = { "bets", "actions", "userName", "currentBalance", "totalOnTable", "totalBet", "roundResults", "positionsTotalAmounts" })
public final class RoulettePlayer_ extends Player implements RoulettePlayer {
	private static final Logger LOGGER = Logger.getLogger(RoulettePlayer_.class.getName());
	private volatile List<RouletteBet> bets;
	private List<BetData> previousBetData;
	private List<PlayerAction> actions;
	private final List<RoundResult> roundResults;
	private Map<Integer, BigDecimal> positionsTotalAmounts;

	public RoulettePlayer_(User user, CasinoTable table) {
		super(user, table);
		bets = new ArrayList<>();
		previousBetData = new ArrayList<>();
		actions = new ArrayList<>(7);
		roundResults = new ArrayList<>();
		positionsTotalAmounts = new HashMap<>();
	}

	@Override
	public void prepareForNextRound() {
		try {
			tryLockOrThrow();
			super.reset();
			if (shouldIncreaseSkips()) {
				increaseSkips();
				if (hasTooManySkips())
					setStatus(PlayerStatus.SIT_OUT);
			} else {
				clearSkips();
				setStatus(PlayerStatus.ACTIVE);
			}
		} finally {
			completePlayerUpdatingAction();
		}
	}

	private boolean shouldIncreaseSkips() {
		return !Functions.isFirstMoreThanSecond_(roundResults.getLast().playerResult().totalBets(), BigDecimal.ZERO);
	}

	private void replicateBet(RouletteBet bet) {
		bets.add(bet.replicate());
	}

	@Override
	public void updateAvailableActions() {
		try {
			getPlayerLock().lock();
			actions = new ArrayList<>();
			actions.add(RoulettePlayerAction.REFRESH);
			if (table.getGamePhase() != RouletteGamePhase.BET || getStatus() != PlayerStatus.ACTIVE) {
				LOGGER.fine("Refresh only allowed. GamePhase:" + table.getGamePhase() + " playerStatus:" + getStatus());
				return;
			}
			if (hasBet()) {
				actions.add(RoulettePlayerAction.REMOVE_LAST_OR_ALL_BETS);
				actions.add(RoulettePlayerAction.REMOVE_BET_FROM_POSITION);
			}
			if (canBetMinimum())
				actions.add(RoulettePlayerAction.BET);
			if (canRepeatLastBets())
				actions.add(RoulettePlayerAction.REPEAT_LAST);
			if (canInvokePlayFunctionality())
				actions.add(RoulettePlayerAction.PLAY);
		} finally {
			getPlayerLock().unlock();
		}
	}

	private boolean canInvokePlayFunctionality() {
		return !bets.isEmpty() && !getTable().isMultiplayer();
	}

	private boolean canRepeatLastBets() {
		return !previousBetData.isEmpty() && canAffordRepetition();
	}

	private boolean canAffordRepetition() {
		return Functions.isFirstMoreOrEqualToSecond_(getCurrentBalance(), calculateSumOfPreviousRoundBets());
	}

	private boolean canBetMinimum() {
		return Functions.isFirstMoreOrEqualToSecond_(getCurrentBalance(), table.getDealer().getGameData().getMinBet());
	}

	@Override
	public List<? extends PlayerAction> getActions() {
		return actions;
	}

	@Override
	public List<RoundResult> getRoundResults() {
		return roundResults;
	}

	@Override
	public void bet(RouletteBet bet, Range<BigDecimal> range) {
		try {
			getPlayerLock().lock();
			if (bet.isCompleted())
				throw new IllegalArgumentException("Bet is completed, cannot add " + bet);
			verifyTotalAmountStaysWithinTableLimits(bet, range.min(), range.max());
			updateBalanceAndTotalBet(bet.getAmount());
			bets.add(bet);
			positionsTotalAmounts.computeIfPresent(bet.getPosition(), (key, val) -> val.add(bet.getAmount()));
			positionsTotalAmounts.putIfAbsent(bet.getPosition(), bet.getAmount());
		} finally {
			completePlayerUpdatingAction();
		}
	}

	@Override
	public void repeatLastBets(Range<BigDecimal> range) {
		try {
			getPlayerLock().lock();
			Verifier.verifyNotEmpty(previousBetData);
			verifyPlayerHasRequiredBalanceForRepetition();
			if (!bets.isEmpty())
				removeBets(true);
			previousBetData.forEach(data -> {
				BetData betData = new BetData(UUID.randomUUID(), data.amount(), data.betType(), data.position());
				bet(new Bet(betData), range);
			});
		} finally {
			completePlayerUpdatingAction();
		}
	}

	private void verifyPlayerHasRequiredBalanceForRepetition() {
		BigDecimal previousSum = calculateSumOfPreviousRoundBets();
		if (Functions.isFirstMoreThanSecond.apply(previousSum, getTotalOnTable()))
			throw new IllegalPlayerActionException("Cannot repeat previous bets. Required=" + previousSum + " totalFunds=" + getTotalOnTable());

	}

	@Override
	public BigDecimal getTotalBet() {
		return calculateAcceptedBetsAmount().setScale(2, RoundingMode.DOWN);
	}

	private void verifyTotalAmountStaysWithinTableLimits(RouletteBet bet, BigDecimal min, BigDecimal max) {
		BigDecimal total = calculatePositionTotalAmount(bet.getPosition());
		BigDecimal totalAttempt = total.add(bet.getAmount());
		if (Functions.isFirstMoreThanSecond.apply(totalAttempt, max))
			throw new IllegalPlayerActionException("Cannot accept bet. CurrentBet " + total + " + " + bet.getAmount() + " would exceed max of " + max);
		if (Functions.isFirstMoreThanSecond.apply(min, totalAttempt))
			throw new IllegalPlayerActionException("Cannot accept bet. CurrentBet " + total + " + " + bet.getAmount() + " would not be minimum " + min);
	}

	@Override
	public void removeBets(Boolean removeAllBets) {
		try {
			tryLockOrThrow();
			Verifier.verifyNotEmpty(this.bets);
			BigDecimal returnAmount;
			if (removeAllBets) {
				returnAmount = calculateAcceptedBetsAmount();
				bets = new ArrayList<>();
				positionsTotalAmounts = new HashMap<>();
			} else {
				RouletteBet bet = bets.remove(bets.size() - 1);
				returnAmount = bet.getAmount();
				positionsTotalAmounts.computeIfPresent(bet.getPosition(), (key, val) -> val.subtract(bet.getAmount()));
				if (isBetPositionTotalAmountZero(bet))
					positionsTotalAmounts.remove(bet.getPosition());
			}
			increaseBalance(returnAmount);
		} finally {
			completePlayerUpdatingAction();
		}
	}

	private boolean isBetPositionTotalAmountZero(RouletteBet bet) {
		if (positionsTotalAmounts.containsKey(bet.getPosition()))
			return positionsTotalAmounts.get(bet.getPosition()).equals(BigDecimal.ZERO);
		return false;
	}

	private void completePlayerUpdatingAction() {
		updateAvailableActions();
		releaseLock();
	}

	private BigDecimal calculateAcceptedBetsAmount() {
		return Functions.calculateSum(bets.stream().map(RouletteBet::getAmount).toList());
	}

	private BigDecimal calculateSumOfPreviousRoundBets() {
		return Functions.calculateSum(previousBetData.stream().map(BetData::amount).toList());
	}

	private BigDecimal calculatePositionTotalAmount(Integer position) {
		return Functions.calculateSum(bets.parallelStream().filter(bet -> bet.getPosition().equals(position)).map(RouletteBet::getAmount).toList());
	}

	@Override
	public void removeBetsFromPosition(Integer position) {
		try {
			tryLockOrThrow();
			Verifier.verifyNotEmpty(this.bets);
			EuropeanRouletteTable.verifyPositionAndGetBetPositionType(position);
			Map<Boolean, List<RouletteBet>> betsInPosition = bets.stream().collect(Collectors.partitioningBy(bet -> bet.getPosition().equals(position)));
			BigDecimal returnAmount = betsInPosition.get(true).stream().map(RouletteBet::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (Functions.isFirstMoreOrEqualToSecond_(BigDecimal.ZERO, returnAmount))
				throw new IllegalPlayerActionException("Should not be called, no bets in position:" + position);
			increaseBalance(returnAmount);
			bets = betsInPosition.get(false);
			positionsTotalAmounts.remove(position);
		} finally {
			completePlayerUpdatingAction();
		}
	}

	public Map<Integer, BigDecimal> getPositionsTotalAmounts() {
		return positionsTotalAmounts;
	}

	@Override
	public List<RouletteBet> getBets() {
		return bets;
	}

	@Override
	public boolean hasBet() {
		return !this.bets.isEmpty();
	}

	@Override
	public BigDecimal getTotalOnTable() {
		return calculateAcceptedBetsAmount().add(getCurrentBalance());
	}

	@Override
	public void updateBalanceAndWinnings(Integer winningNumber) {
		try {
			tryLockOrThrow();
			bets.forEach(bet -> bet.complete(winningNumber));
			BigDecimal totalWinnings = Functions.calculateSum(bets.parallelStream().map(RouletteBet::getWinAmount).toList());
			increaseBalance(totalWinnings);
		} finally {
			releaseLock();
		}
	}

	@Override
	public void onRoundCompletion(SpinResult spinResult) {
		try {
			getPlayerLock().lock();
			if (!bets.isEmpty())
				saveRoundBetData();
			List<RouletteBet> winningBets = bets.stream().filter(RouletteBet::success).toList();
			List<RouletteBet> losingBets = bets.stream().filter(bet -> !bet.success()).toList();
			BigDecimal totalBetsAmount = Functions.calculateSum(bets.stream().map(RouletteBet::getAmount).toList());
			BigDecimal totalRemainingBetsAmount = Functions.calculateSum(winningBets.parallelStream().map(RouletteBet::getAmount).toList());
			BigDecimal totalWinningsAmount = Functions.calculateSum(winningBets.stream().map(RouletteBet::getWinAmount).toList());
			bets.clear();
			handleWinningBets(winningBets);
			updateRoundResult(spinResult, winningBets, losingBets, totalBetsAmount, totalRemainingBetsAmount, totalWinningsAmount);
			updatePositionTotalAmountsWithExistingBets(losingBets);
		} finally {
			getPlayerLock().unlock();
		}
	}

	private void updateRoundResult(SpinResult spinResult, List<RouletteBet> winningBets, List<RouletteBet> losingBets, BigDecimal totalBetsAmount, BigDecimal totalRemainingBetsAmount, BigDecimal totalWinningsAmount) {
		var roundResult = createRoundResults(totalBetsAmount, spinResult, winningBets, totalWinningsAmount, losingBets, totalRemainingBetsAmount);
		roundResults.add(roundResult);
	}

	private void handleWinningBets(List<RouletteBet> winningBets) {
		if (isActive())
			winningBets.forEach(this::replicateBet);
		else
			transferWinningBetsToBalance(winningBets);
	}

	private RoundResult createRoundResults(BigDecimal totalBetsAmount, SpinResult spinResult, List<RouletteBet> winningBets, BigDecimal totalWinningsAmount, List<RouletteBet> losingBets, BigDecimal totalRemainingBetsAmount) {
		var playerResult = new PlayerResult(winningBets, losingBets, totalBetsAmount, totalRemainingBetsAmount, totalWinningsAmount, getCurrentBalance(), getId());
		return new RoundResult(spinResult, playerResult, getId());
	}

	private void updatePositionTotalAmountsWithExistingBets(List<RouletteBet> losingBets) {
		positionsTotalAmounts = new HashMap<>();
		bets.forEach(bet -> {
			positionsTotalAmounts.computeIfPresent(bet.getPosition(), (key, val) -> val.add(bet.getAmount()));
			positionsTotalAmounts.putIfAbsent(bet.getPosition(), bet.getAmount());
		});
	}

	private void transferWinningBetsToBalance(List<RouletteBet> winningBets) {
		increaseBalance(Functions.calculateSum(winningBets.parallelStream().map(RouletteBet::getAmount).toList()));
	}

	private void saveRoundBetData() {
		previousBetData = bets.stream().map(RouletteBet::getData).toList();
	}

}
