package com.casino.common.dealer;

import com.casino.common.message.Event;
import com.casino.common.message.MessageTitle;
import com.casino.common.player.ICasinoPlayer;

public interface CommunicationChannel {
	void notifyPlayerArrival(ICasinoPlayer player);

	void notifyPlayer(Event title, ICasinoPlayer player);

	<T> void notifyPlayerWithCustomMessage(ICasinoPlayer player, T message);
	void notifyAll(MessageTitle title, ICasinoPlayer player);

	void notifyTableOpening(ICasinoPlayer watcher);
}
