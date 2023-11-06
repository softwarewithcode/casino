package com.casino.common.validation;

import java.util.List;
import java.util.UUID;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.table.structure.CasinoTable;

public final class Verifier {

	public static UUID verifyIdStructure(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id missing");
		}
		UUID validId = UUID.fromString(id);
		if (validId.toString().equals(id))
			return validId;
		throw new IllegalArgumentException("not a valid id " + id + " component produced:" + validId);
	}

	public static void verifyGamePhase(CasinoTable table, GamePhase expectedGamePhase) {
		if (table == null || expectedGamePhase == null)
			throw new IllegalPlayerActionException("Not valid table or gamePhase not expected " + table + " phase:" + expectedGamePhase);
		if (table.getGamePhase() != expectedGamePhase)
			throw new IllegalPlayerActionException("Wrong gamePhase, expected:" + expectedGamePhase + " was:" + table.getGamePhase());
	}

	public static void verifyId(UUID actualId, UUID expectedId) {
		if (actualId == null || expectedId == null)
			throw new IllegalArgumentException("Not valid id:" + actualId + " expected:" + expectedId);
		if (!actualId.equals(expectedId))
			throw new IllegalArgumentException("Id mismatch, actual:" + actualId + " expected:" + expectedId);
	}

	public static void verifyNotNull(Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("Parameter missing:");
	}

	public static void verifyNotEmpty(List<?> list) {
		if (list == null)
			throw new IllegalPlayerActionException("cannot operate on missing data");
		if (list.isEmpty())
			throw new IllegalPlayerActionException("cannot operate on empty data");
	}
}
