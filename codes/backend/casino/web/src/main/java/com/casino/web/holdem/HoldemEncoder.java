package com.casino.web.holdem;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HoldemEncoder implements Encoder.Text<HoldemMessage> {
	private static final Logger LOGGER = Logger.getLogger(HoldemEncoder.class.getName());
	private static final JsonbConfig config; // not-thread-safe
	static {
		config = new JsonbConfig();
	}
	private static final Jsonb jsonb = JsonbBuilder.create(config);

	@Override
	public String encode(HoldemMessage object) throws EncodeException {
		try {
			return jsonb.toJson(object, HoldemMessage.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}

}
