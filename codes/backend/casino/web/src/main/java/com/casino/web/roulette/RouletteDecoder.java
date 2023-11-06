package com.casino.web.roulette;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

import jakarta.websocket.Decoder;

public class RouletteDecoder implements Decoder.Text<RouletteMessage> {
	private static final Logger LOGGER = Logger.getLogger(RouletteDecoder.class.getName());

	@Override
	public RouletteMessage decode(String message) {
		try {
			return MessageConverter.convertToType(message, RouletteMessage.class);
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
