package com.casino.poker.message;

import com.casino.common.message.Event;
import com.casino.common.message.Mapper;
import com.casino.common.table.structure.ICasinoTable;
import com.casino.poker.player.PokerPlayer;

public class PokerMapper extends Mapper {
	public static String createHoleCardsMessage(Event title, ICasinoTable table, PokerPlayer player) {
		if (skipSerialization)
			return "serialization switched off";
		PokerMessage message = new PokerMessage(player.getHoleCards());
		message.title = title;
		message.player = player;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}
}
