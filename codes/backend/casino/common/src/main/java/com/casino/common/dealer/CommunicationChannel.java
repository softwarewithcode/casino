package com.casino.common.dealer;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ICasinoTable;

/**
 * @author softwarewithcode from GitHub
 * 
 */
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
		table.getPlayers().parallelStream().forEach(casinoPlayer -> casinoPlayer.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(casinoPlayerEntry -> casinoPlayerEntry.getValue().sendMessage(message));
//		});
	}

	public <T> void multicast(T message, ICasinoPlayer excludedPlayer) {
		table.getPlayers().parallelStream().filter(casinoPlayer -> !casinoPlayer.getId().equals(excludedPlayer.getId())).forEach(casinoPlayer -> casinoPlayer.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(casinoPlayerEntry -> casinoPlayerEntry.getValue().sendMessage(message));
//		});
	}
}
