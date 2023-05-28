package com.casino.common.dealer;

import com.casino.common.message.Mapper;
import com.casino.common.message.MessageTitle;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.structure.ICasinoTable;
import com.casino.common.message.Event;

/**
 * @author softwarewithcode from GitHub
 *
 */
public class Notifier implements CommunicationChannel {
	private final ICasinoTable table;

	public Notifier(ICasinoTable table) {
		super();
		this.table = table;
	}

	private <T, P extends ICasinoPlayer> void unicast(T message, P player) {
		if (player != null)
			player.sendMessage(message);
	}

	private <T> void broadcast(T message) {
		table.getPlayers().parallelStream().forEach(casinoPlayer -> casinoPlayer.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(casinoPlayerEntry -> casinoPlayerEntry.getValue().sendMessage(message));
//		});
	}

	private <T> void multicast(T message, ICasinoPlayer excludedPlayer) {
		table.getPlayers().parallelStream().filter(casinoPlayer -> !casinoPlayer.getId().equals(excludedPlayer.getId())).forEach(casinoPlayer -> casinoPlayer.sendMessage(message));
//		Thread.ofVirtual().start(() -> {
		table.getWatchers().entrySet().parallelStream().forEach(casinoPlayerEntry -> casinoPlayerEntry.getValue().sendMessage(message));
//		});
	}

	@Override
	public void notifyPlayerArrival(ICasinoPlayer player) {
		String loginMessage = Mapper.createMessage(Event.LOGIN, table, player);
		unicast(loginMessage, player);
		String commonMessage = Mapper.createMessage(Event.NEW_PLAYER, table, player);
		multicast(commonMessage, player);
	}

	@Override
	public void notifyPlayer(Event title, ICasinoPlayer player) {
		String message = Mapper.createMessage(title, table, player);
		unicast(message, player);
	}

	@Override
	public <T> void notifyPlayerWithCustomMessage(ICasinoPlayer player, T message) {
		unicast(message, player);
	}

	@Override
	public void notifyAll(MessageTitle title, ICasinoPlayer player) {
		String message = Mapper.createMessage(title, table, player);
		broadcast(message);
	}

	@Override
	public void notifyTableOpening(ICasinoPlayer watcher) {
		String message = Mapper.createMessage(Event.OPEN_TABLE, table, null);
		unicast(message, watcher);
	}

}
