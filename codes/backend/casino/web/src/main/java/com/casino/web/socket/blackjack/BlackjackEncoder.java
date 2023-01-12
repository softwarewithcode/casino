package com.casino.web.socket.blackjack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.web.Message;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class BlackjackEncoder implements Encoder.Text<Message> {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEncoder.class.getName());
	private static final JsonbConfig config; // not-thread-safe
	static {
		config = new JsonbConfig();
	}
	private static Jsonb jsonb = JsonbBuilder.create(config);

	@Override
	public String encode(Message object) throws EncodeException {
		try {
			return jsonb.toJson(object, Message.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}

}
