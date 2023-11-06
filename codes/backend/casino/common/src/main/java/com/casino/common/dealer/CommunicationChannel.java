package com.casino.common.dealer;

import com.casino.common.message.Event;
import com.casino.common.message.MessageTitle;
import com.casino.common.user.Connectable;

public interface CommunicationChannel {
	void notifyPlayerArrival(Connectable player);

	void notifyEvent(Event title, Connectable toWhom);

	<T> void notifyMessage(Connectable toWhom, T message);
	void notifyEverybody(MessageTitle title, Connectable toWhom);

	void notifyPrivateData(Connectable watcher);
	
	void notifyUpdate();
}
