package com.casino.poker.message;

import com.casino.common.message.Event;
import com.casino.common.message.Mapper;
import com.casino.common.runner.RunMode;
import com.casino.common.table.structure.CasinoTable;
import com.casino.poker.player.PokerPlayer;

public class PokerMapper extends Mapper {
	public static String createHoleCardsMessage(Event title, CasinoTable table, PokerPlayer player) {
		if (RunMode.isTestMode())
			return "serialization switched off";
		PokerMessage message = new PokerMessage(player.getHoleCards());
		message.title = title;
		message.player = player;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}
}
