package com.casino.web.blackjack;

import jakarta.websocket.Decoder;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

public class BlackjackMessageDecoder implements Decoder.Text<BlackjackMessage> {
	private static final Logger LOGGER = Logger.getLogger(BlackjackMessageDecoder.class.getName());

	@Override
	public BlackjackMessage decode(String message) {
		try {
			return MessageConverter.convertToType(message, BlackjackMessage.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, " decode failed ", e);
		}
		throw new IllegalArgumentException("could not decode input");
	}

	@Override
	public boolean willDecode(String s) {
		return s != null;
	}

}
