package com.casino.common.table;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.casino.common.game.Game;
import com.casino.common.game.GameData;
import com.casino.common.language.Language;
import com.casino.common.table.structure.TableType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties(value = { "thresholds", "gameData", "id", "language", "game", "type", "availablePositions" })
public class TableCard<T extends GameData> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	private final TableThresholds thresholds;
	private final T gameData;
	private final UUID id;
	private final Language language;
	private final Game game;
	private final TableType type;
	private List<Integer> availablePositions;

	public TableCard(TableData tableData, T gameData) {
		this.thresholds = tableData.thresholds();
		this.id = tableData.id();
		this.language = tableData.language();
		this.game = tableData.game();
		this.type = tableData.tableType();
		this.gameData = gameData;
	}

	public TableThresholds getThresholds() {
		return thresholds;
	}

	public UUID getId() {
		return id;
	}

	public Game getGame() {
		return game;
	}

	public void setAvailablePositions(List<Integer> availablePositions) {
		this.availablePositions = availablePositions;
	}

	public TableType getType() {
		return type;
	}

	public T getGameData() {
		return gameData;
	}

	// getter for serialization
	public Language getLanguage() {
		return language;
	}

	@JsonProperty // getter for serialization
	public List<Integer> getAvailablePositions() {
		return availablePositions;
	}
}
