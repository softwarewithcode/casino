package com.casino.common.dealer;

import com.casino.common.message.Event;
import com.casino.common.message.Mapper;
import com.casino.common.message.MessageTitle;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.user.Connectable;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author softwarewithcode from GitHub
 */
public class Notifier implements CommunicationChannel {
	private final CasinoTable table;
	private static final Logger LOGGER = Logger.getLogger(Notifier.class.getName());

	public Notifier(CasinoTable table) {
		super();
		this.table = table;
	}

	private <T, C extends Connectable> void unicast(T message, C connectable) {
		if (connectable != null)
			Thread.ofVirtual().start(() -> connectable.sendMessage(message));
	}

	private <T> void broadcast(T message) {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Notifier broadcasting " + message + " table:" + table);
		Thread.ofVirtual().start(() -> sendMessageToEverybody(message));
	}

	private <T> void sendMessageToEverybody(T message) {
		var players = table.getPlayers().parallelStream().toList();
		//Some watchers might receive message before the players
		Thread.ofVirtual().start(() -> players.forEach(player -> player.sendMessage(message)));
		var watchers = table.getWatchers().entrySet().parallelStream().toList();
		Thread.ofVirtual().start(() -> watchers.forEach(watcher -> watcher.getValue().sendMessage(message)));
	}

	private <T> void multicast(T message, Connectable excludedConnectable) {
		var players = table.getPlayers().parallelStream().filter(casinoPlayer -> !casinoPlayer.getId().equals(excludedConnectable.getId())).toList();
		Thread.ofVirtual().start(() -> players.forEach(casinoPlayer -> casinoPlayer.sendMessage(message)));
		var watchers = table.getWatchers().entrySet().parallelStream().toList();
		Thread.ofVirtual().start(() -> watchers.forEach(casinoPlayerEntry -> casinoPlayerEntry.getValue().sendMessage(message)));
	}

	@Override
	public void notifyPlayerArrival(Connectable player) {
		String loginMessage = Mapper.createMessage(Event.LOGIN, table, player);
		unicast(loginMessage, player);
		String commonMessage = Mapper.createMessage(Event.NEW_PLAYER, table, player);
		multicast(commonMessage, player);
	}

	@Override
	public void notifyEvent(Event title, Connectable connectable) {
		Objects.requireNonNull(title);
		Objects.requireNonNull(connectable);
		String message = Mapper.createMessage(title, table, connectable);
		unicast(message, connectable);
	}

	@Override
	public <T> void notifyMessage(Connectable player, T message) {
		unicast(message, player);
	}

	@Override
	public void notifyEverybody(MessageTitle title, Connectable relatedTargetIfAny) {
		String message = Mapper.createMessage(title, table, relatedTargetIfAny);
		broadcast(message);
	}

	@Override
	public void notifyPrivateData(Connectable connectable) {
		String message = Mapper.createMessage(Event.OPEN_TABLE, table, connectable);
		unicast(message, connectable);
	}

	@Override
	public void notifyUpdate() {
		String message = Mapper.createMessage(Event.STATUS_UPDATE, table, null);
		broadcast(message);
	}

}
