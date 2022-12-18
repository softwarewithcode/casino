package com.casino.common.dealer;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;

public class CommunicationChannel {
	private final ICasinoTable table;

	public CommunicationChannel(ICasinoTable table) {
		super();
		this.table = table;
	}

	public <T, P extends ICasinoPlayer> void unicast(T message, P player) {
		if (player != null)
			player.sendMessage(message);
	}

	public <T> void broadcast(T message) {
		table.getPlayers().parallelStream().forEach(player -> player.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(o -> o.getValue().sendMessage(message));
//		});
	}

	public <T> void multicast(T message, ICasinoPlayer excludedPlayer) {
		table.getPlayers().parallelStream().filter(player -> !player.getId().equals(excludedPlayer.getId())).forEach(player -> player.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(entry -> entry.getValue().sendMessage(message));
//		});
	}
}
