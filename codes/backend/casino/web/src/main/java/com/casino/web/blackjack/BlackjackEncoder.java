package com.casino.web.blackjack;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class BlackjackEncoder implements Encoder.Text<BlackjackMessage> {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEncoder.class.getName());
	private static final JsonbConfig config; // not-thread-safe
	static {
		config = new JsonbConfig();
	}
	private static final Jsonb jsonb = JsonbBuilder.create(config);

	@Override
	public String encode(BlackjackMessage object) throws EncodeException {
		try {
			return jsonb.toJson(object, BlackjackMessage.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}

}
