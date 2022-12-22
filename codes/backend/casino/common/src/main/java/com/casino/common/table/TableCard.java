package com.casino.common.table;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.casino.common.language.Language;

public class TableCard implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Thresholds thresholds;
	private final UUID id;
	private final Language language;
	private final Game game;
	private List<Integer> availablePositions;

	public TableCard(TableInitData initData) {
		super();
		this.thresholds = initData.thresholds();
		this.id = initData.id();
		this.language = initData.language();
		this.game = initData.game();
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	public UUID getId() {
		return id;
	}

	public Game getGame() {
		return game;
	}

	public Language getLanguage() {
		return language;
	}

	public List<Integer> getAvailablePositions() {
		return availablePositions;
	}

	public void setAvailablePositions(List<Integer> availablePositions) {
		this.availablePositions = availablePositions;
	}

}
