package com.casino.common.message;

import java.io.Serial;
import java.io.Serializable;

import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.structure.ICasinoTable;

public class Message implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	public ICasinoTable table;
	public MessageTitle title;
	public ICasinoPlayer player;

	public ICasinoTable getTable() {
		return table;
	}

	public void setTable(ICasinoTable table) {
		this.table = table;
	}

	public ICasinoPlayer getPlayer() {
		return player;
	}

	public void setPlayer(CasinoPlayer player) {
		this.player = player;
	}

}
