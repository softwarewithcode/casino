package com.casino.web.holdem;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

public class HoldemEncoder implements Encoder.Text<HoldemMessage> {
	private static final Logger LOGGER = Logger.getLogger(HoldemEncoder.class.getName());

	@Override
	public String encode(HoldemMessage object) throws EncodeException {
		try {
			return MessageConverter.convertToString(object);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}

}
