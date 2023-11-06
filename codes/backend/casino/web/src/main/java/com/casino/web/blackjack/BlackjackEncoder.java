package com.casino.web.blackjack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class BlackjackEncoder implements Encoder.Text<BlackjackMessage> {
	private static final Logger LOGGER = Logger.getLogger(BlackjackEncoder.class.getName());

	@Override
	public String encode(BlackjackMessage object) throws EncodeException {
		try {
			return MessageConverter.convertToString(object);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Encoding error:", e);
		}
		throw new IllegalArgumentException("Could not encode message");
	}

}
