package com.casino.common.dealer;

import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.ReentrantLock;

import com.casino.common.message.Event;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.Table;
import com.casino.common.user.Connectable;

public abstract class TableGameCroupier implements Croupier {
	protected final CommunicationChannel voice;
	protected final ReentrantLock croupierLock;

	protected TableGameCroupier(Table table) {
		this.voice = new Notifier(table);
		croupierLock = new ReentrantLock(true);
	}

	protected abstract void startGame();

	protected boolean canStartGame() {
		// is it the game which is waiting players or the table? Both? Separate GameStatus?
		return getTable().getStatus() == TableStatus.WAITING_PLAYERS && !getTable().getPlayers().isEmpty();
	}

	@Override
	public void onWatcherArrival(Connectable watcher) {
		voice.notifyPrivateData(watcher); // opens table for watcher
		
	}

	protected void tryLockOrThrow() {
		if (!croupierLock.tryLock())
			throw new ConcurrentModificationException("croupierLock was not obtained. Timing error" + croupierLock + " t:" + Thread.currentThread());
	}

	@Override
	public <T extends CasinoPlayer> void onPlayerArrival(T player) {
		try {
			player.setStatus(PlayerStatus.ACTIVE);
			voice.notifyPlayerArrival(player);
			if (!croupierLock.tryLock()) {
				voice.notifyEvent(Event.STATUS_UPDATE, player);
				return;
			}
			if (canStartGame())
				startGame();

		} finally {
			if (croupierLock.isHeldByCurrentThread())
				croupierLock.unlock();
		}
	}

	@Override
	public void refresh(Connectable connectable) {
		Thread.ofVirtual().start(() -> voice.notifyEvent(Event.STATUS_UPDATE, connectable));
	}
}
