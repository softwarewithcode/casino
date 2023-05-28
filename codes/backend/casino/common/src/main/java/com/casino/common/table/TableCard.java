package com.casino.common.table;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.casino.common.game.Game;
import com.casino.common.game.GameData;
import com.casino.common.language.Language;
import com.casino.common.table.structure.TableType;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIncludeProperties(value = { "thresholds", "gameData", "id", "language", "game","type","availablePositions"})
public class TableCard implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	private final TableThresholds thresholds;
	private final GameData gameData;
	private final UUID id;
	private final Language language;
	private final Game game;
	private final TableType type;
	private List<Integer> availablePositions;

	public TableCard(TableData initData, GameData gameData) {
		super();
		this.thresholds = initData.thresholds();
		this.id = initData.id();
		this.language = initData.language();
		this.game = initData.game();
		this.type = initData.tableType();
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

	public GameData getGameData() {
		return gameData;
	}
	// getter for serialization
	public Language getLanguage() {
		return language;
	}
	@JsonProperty// getter for serialization
	public List<Integer> getAvailablePositions() {
		return availablePositions;
	}
}
