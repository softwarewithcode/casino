package com.casino.common.message;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.structure.ICasinoTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Mapper {
	private static final Logger LOGGER = Logger.getLogger(Mapper.class.getName());
	public static final String JUNIT_RUNNER = "JUNIT_RUNNER";
	protected static final boolean skipSerialization;

	static {
		Object junitRunner = System.getProperties().get(JUNIT_RUNNER);
		skipSerialization = junitRunner != null;
	}

	public static String convertToJSON(Message message) {
		ObjectMapper mapper = JsonMapper.builder().build();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, "Message could not be converted to json:" + message, e);
		}
		throw new RuntimeException("Message could not be converted to JSON:" + message);
	}

	public static String createMessage(MessageTitle title, ICasinoTable table, ICasinoPlayer operand) {
		if (skipSerialization)
			return "serialization switched off";
		Message message = new Message();
		message.title = title;
		message.player = operand;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}

}
