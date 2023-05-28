package com.casino.common.table;

public record TableThresholds(

		Integer minPlayers, Integer maxPlayers, Integer seatCount) {
	// compact constructor
	public TableThresholds {
		if (minPlayers < 0)
			throw new IllegalArgumentException("Minimum players cannot be less than zero");
		if (minPlayers > maxPlayers)
			throw new IllegalArgumentException("minPlayers cannot be bigger that maxPlayers");
		if (seatCount < maxPlayers)
			throw new IllegalArgumentException("not enough seats for players");
	}
}
