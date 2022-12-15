package com.casino.blackjack.message;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.user.Title;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Mapper {
	public static final String JUNIT_RUNNER = "JUNIT_RUNNER";
	private static final boolean skipSerialization;

	static {
		Object junitRunner = System.getProperties().get(JUNIT_RUNNER);
		if (junitRunner != null)
			skipSerialization = true;
		else
			skipSerialization = false;
	}

	private static String convertToJSON(Message message) {
		ObjectMapper mapper = JsonMapper.builder().build();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String createMessage(Title title, BlackjackTable table, BlackjackPlayer operand) {
		if (skipSerialization)
			return "serialization switched off";
		Message message = new Message();
		message.title = title;
		message.player = operand;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}
}
