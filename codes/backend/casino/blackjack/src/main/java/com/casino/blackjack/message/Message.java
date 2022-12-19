package com.casino.blackjack.message;

import java.io.Serializable;

import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.CasinoTable;
import com.casino.common.user.Title;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	public CasinoTable table;
	public Title title;
	public CasinoPlayer player;

	public CasinoTable getTable() {
		return table;
	}

	public void setTable(CasinoTable table) {
		this.table = table;
	}

	public Title getTitle() {
		return title;
	}

	public void setTitle(Title title) {
		this.title = title;
	}

	public CasinoPlayer getPlayer() {
		return player;
	}

	public void setPlayer(CasinoPlayer player) {
		this.player = player;
	}

}
