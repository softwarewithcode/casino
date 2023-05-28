package com.casino.web.holdem;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.websocket.Decoder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HoldemDecoder implements Decoder.Text<HoldemMessage> {
    private final JsonbConfig config = new JsonbConfig();
    private Jsonb jsonb = JsonbBuilder.create(config);
    private static final Logger LOGGER = Logger.getLogger(HoldemDecoder.class.getName());

    @Override
    public HoldemMessage decode(String message) {
        try {
            return jsonb.fromJson(message, HoldemMessage.class);
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
