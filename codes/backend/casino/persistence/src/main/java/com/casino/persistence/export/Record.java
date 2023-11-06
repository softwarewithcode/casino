package com.casino.persistence.export;

import java.util.UUID;

public interface Record {

	UUID playerId();

	UUID tableId();

	Integer gameId();

	String json(); // bson, xml?
}
