package com.casino.blackjack.message;

import java.io.Serializable;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.user.Title;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	public BlackjackTable table;
	public Title title;
	public BlackjackPlayer player;

	public BlackjackTable getTable() {
		return table;
	}

	public void setTable(BlackjackTable table) {
		this.table = table;
	}

	public Title getTitle() {
		return title;
	}

	public void setTitle(Title title) {
		this.title = title;
	}

	public BlackjackPlayer getPlayer() {
		return player;
	}

	public void setPlayer(BlackjackPlayer player) {
		this.player = player;
	}

}
