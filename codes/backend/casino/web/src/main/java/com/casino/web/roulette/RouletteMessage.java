package com.casino.web.roulette;

import java.util.List;
import java.util.UUID;

import com.casino.common.game.Game;
import com.casino.common.message.Event;
import com.casino.roulette.export.BetPosition;
import com.casino.roulette.export.EuropeanRouletteTable;
import com.casino.roulette.export.RoulettePlayerAction;
import com.casino.web.common.ClientMessage;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = { "game", "action", "amount", "removeAllBets", "tableBetPositions", "title", "position", "spinId" })
public class RouletteMessage extends ClientMessage {
	private RoulettePlayerAction action;
	private Event title;
	private Integer position;
	private Boolean removeAllBets;
	private UUID spinId;
	private List<BetPosition> tableBetPositions = EuropeanRouletteTable.BET_POSITIONS;

	public RoulettePlayerAction getAction() {
		return action;
	}

	public void setAction(RoulettePlayerAction action) {
		this.action = action;
	}

	public Event getTitle() {
		return title;
	}

	public void setTitle(Event title) {
		this.title = title;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Boolean getRemoveAllBets() {
		return removeAllBets;
	}

	public void setRemoveAllBets(Boolean removeAllBets) {
		this.removeAllBets = removeAllBets;
	}

	public UUID getSpinId() {
		return spinId;
	}

	public void setSpinId(UUID spinId) {
		this.spinId = spinId;
	}

	public List<BetPosition> getTableBetPositions() {
		return tableBetPositions;
	}

	public void setTableBetPositions(List<BetPosition> tableBetPositions) {
		this.tableBetPositions = tableBetPositions;
	}

	@Override
	public Game getGame() {
		return Game.ROULETTE;
	}

}
