package com.casino.blackjack.message;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.user.Title;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Mapper {
	public static final String JUNIT_RUNNER = "JUNIT_RUNNER";
	private static final boolean skipSerialzation;
	static {
		Object junitRunner = System.getProperties().get(JUNIT_RUNNER);
		if (junitRunner != null)
			skipSerialzation = true;
		else
			skipSerialzation = false;
	}

	private static String convertToJSON(Message message) {
		JsonMapper.builder().disable(MapperFeature.USE_ANNOTATIONS);
		ObjectMapper mapper = JsonMapper.builder().build();
//		String tableJson = objectMapper.convertValue(table, BlackjackTable.class);
		try {
			return mapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String createArrivalMessage(BlackjackTable table, BlackjackPlayer player) {
		if (skipSerialzation)
			return "serialization switched off";
		Message message = new Message();
		message.setTitle(Title.NEW_PLAYER);
		message.player = player;
		message.setTable(table);
		return Mapper.convertToJSON(message);
	}
}
