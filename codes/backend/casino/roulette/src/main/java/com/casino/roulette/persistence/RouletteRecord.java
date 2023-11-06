package com.casino.roulette.persistence;

import java.time.Instant;
import java.util.UUID;

import com.casino.common.game.Game;
import com.casino.persistence.export.Record;

public class RouletteRecord implements Record {
	private final RoundResult roundResult;
	private final Instant instant;

	public RouletteRecord(RoundResult roundResult) {
		instant = Instant.now();
		this.roundResult = roundResult;
	}

	@Override
	public UUID playerId() {
		return roundResult.playerResult().playerId();
	}

	@Override
	public UUID tableId() {
		return roundResult.tableId();
	}

	public Instant getInstant() {
		return instant;
	}

	@Override
	public Integer gameId() {
		return Game.ROULETTE.getGameNumber();
	}

	@Override
	public String json() {
		throw new RuntimeException("json, bson, xml ?");
	}

	public RoundResult getRoundResult() {
		return roundResult;
	}

}
