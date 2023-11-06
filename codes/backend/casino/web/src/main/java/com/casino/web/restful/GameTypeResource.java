package com.casino.web.restful;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/gametypes")
public class GameTypeResource {
	private static final Logger LOGGER = Logger.getLogger(GameTypeResource.class.getName());
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private List<GameType> types = List.of(new GameType(0, "Blackjack"), new GameType(1, "Texas Hold'em cash"),
			new GameType(2, "Roulette"));

	@GET
	@Produces("application/json")
	public Response fetchGameTypes() {
		try {
			var json = MAPPER.writeValueAsString(types);
			return Response.ok(json).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Resource error ", e);
		}
		return Response.serverError().build();
	}

	@JsonIncludeProperties(value = { "index", "type" })
	private class GameType {
		private final int index;
		private final String type;

		public GameType(int dbIndex, String englishName) {
			super();
			this.index = dbIndex;
			this.type = englishName;
		}

		public int getIndex() {
			return index;
		}

		public String getType() {
			return type;
		}

	}
}
