package com.casino.blackjack.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Mapper {

	public static String convertTable(BlackjackTable table) {
		ObjectMapper mapper = new ObjectMapper();
//		String tableJson = objectMapper.convertValue(table, BlackjackTable.class);
		String t = null;
		try {
			t = mapper.writeValueAsString(table);
			System.out.println("B");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}
}
