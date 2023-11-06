package com.casino.web.holdem;

import jakarta.websocket.Decoder;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.message.MessageConverter;

public class HoldemDecoder implements Decoder.Text<HoldemMessage> {
	private static final Logger LOGGER = Logger.getLogger(HoldemDecoder.class.getName());

	@Override
	public HoldemMessage decode(String message) {
		try {
			return MessageConverter.convertToType(message, HoldemMessage.class);
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
