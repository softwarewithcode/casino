package com.casino.web.restful;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.game.GameData;
import com.casino.common.table.TableCard;
import com.casino.service.table.RouletteTableService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/roulette")
public class RouletteTableResource {
	@Inject
	private RouletteTableService rouletteTableService;
	private static final Logger LOGGER = Logger.getLogger(RouletteTableResource.class.getName());
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@GET
	@Path("/tables")
	@Produces("application/json")
	public Response fetchTableCards() {
		try {
			List<TableCard<? extends GameData>> cards = rouletteTableService.fetchTableCards(0, 100);
			var json = MAPPER.writeValueAsString(cards);
			return Response.ok(json).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Resource error ", e);
		}
		return Response.serverError().build();
	}
}