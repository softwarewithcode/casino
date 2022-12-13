package com.casino.web.endpoint.blackjack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.web.Message;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class BlackjackMessageDecoder implements Decoder.Text<Message> {
	private final JsonbConfig config = new JsonbConfig();
	private Jsonb jsonb = JsonbBuilder.create(config);
	private static final Logger LOGGER = Logger.getLogger(BlackjackMessageDecoder.class.getName());

	@Override
	public Message decode(String message) throws DecodeException {
		try {
			return jsonb.fromJson(message, Message.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, " decode failed ", e);
		}
		throw new IllegalArgumentException("could not decode input");
	}

	@Override
	public boolean willDecode(String s) {
		// TODO Auto-generated method stub
		return s != null;
	}

}
