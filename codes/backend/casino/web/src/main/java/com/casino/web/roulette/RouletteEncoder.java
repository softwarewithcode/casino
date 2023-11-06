package com.casino.web.roulette;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class RouletteEncoder implements Encoder.Text<RouletteMessage> {
	private static final Logger LOGGER = Logger.getLogger(RouletteEncoder.class.getName());

	@Override
	public String encode(RouletteMessage object) throws EncodeException {
		try {
			return  MessageConverter.convertToString(object);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}
}
