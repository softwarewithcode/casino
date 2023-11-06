package com.casino.common.message;

import java.io.Serial;
import java.io.Serializable;

import com.casino.common.game.Game;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.user.Connectable;

public class Message implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	public CasinoTable table;
	public MessageTitle title;
	public Connectable player;
	public CasinoTable getTable() {
		return table;
	}

	public void setTable(CasinoTable table) {
		this.table = table;
	}

	public Connectable getPlayer() {
		return player;
	}

	public Game getGame() {
		return table.getDealer().getGameData().getGame();
	}


}
