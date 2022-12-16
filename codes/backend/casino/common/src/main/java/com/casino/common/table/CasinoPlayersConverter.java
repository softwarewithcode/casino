package com.casino.common.table;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.casino.common.player.CasinoPlayer;
import com.fasterxml.jackson.databind.util.StdConverter;

public class CasinoPlayersConverter extends StdConverter<Map<UUID, CasinoPlayer>, Collection<CasinoPlayer>> {

	@Override
	public Collection<CasinoPlayer> convert(Map<UUID, CasinoPlayer> players) {
		Object e = players.entrySet();
		return players.values();
	}
}