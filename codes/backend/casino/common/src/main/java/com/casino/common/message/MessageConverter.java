package com.casino.common.message;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class MessageConverter {
	private static final ObjectMapper mapper;
	private static final Logger LOGGER = Logger.getLogger(MessageConverter.class.getName());
	static {
		mapper = new ObjectMapper();
	}

	private MessageConverter() {
		// usage through static methods
	}

	public static <T> String convertToString(T from) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(from);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, "Conversion error", e);
		}
		throw new RuntimeException("Cannot convert from " + from + " to String ");
	}

	public static <T> T convertToType(String from, Class<T> to) {
		try {
			return mapper.readValue(from, to);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, "Conversion error", e);
		}
		throw new RuntimeException("Cannot convert " + from + " to " + to);
	}

}
