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
		ICasinoPlayer p = table.getPlayers().get(player.getId());
		if (p != null)
			p.sendMessage(message);
	}

	public <T> void broadcast(T message) {
		table.getPlayers().entrySet().parallelStream().forEach(entry -> entry.getValue().sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(o -> o.getValue().sendMessage(message));
//		});
	}

	public <T> void multicast(T message, ICasinoPlayer excludeEndpoint) {
		table.getPlayers().entrySet().parallelStream().filter(entry -> !entry.getKey().equals(excludeEndpoint.getId())).forEach(player -> player.getValue().sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(entry -> entry.getValue().sendMessage(message));
//		});
	}
}
