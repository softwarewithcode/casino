package com.casino.common.message;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.runner.RunMode;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.user.Connectable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Mapper {
	private static final Logger LOGGER = Logger.getLogger(Mapper.class.getName());
	public static final ObjectMapper MAPPER;

	static {
		MAPPER = JsonMapper.builder().build();
	}

	public static String convertToJSON(Message message) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(message);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, "Message could not be converted to json:" + message, e);
		}
		throw new RuntimeException("Message could not be converted to JSON:" + message);
	}

	public static String createMessage(MessageTitle title, CasinoTable table, Connectable operand) {
		if (RunMode.isTestMode())
			return "serialization switched off";
		Message message = new Message();
		message.title = title;
		message.player = operand;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}

}
