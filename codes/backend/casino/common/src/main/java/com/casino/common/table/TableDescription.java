package com.casino.common.table;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.casino.common.language.Language;

public class TableDescription implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Thresholds thresholds;
	private final UUID id;
	private final Language language;
	private List<Integer> availablePositions;

	public TableDescription(Thresholds thresholds, UUID id, Language language) {
		super();
		this.thresholds = thresholds;
		this.id = id;
		this.language = language;
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	public UUID getId() {
		return id;
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
