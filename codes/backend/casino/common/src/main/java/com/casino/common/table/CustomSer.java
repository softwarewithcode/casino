package com.casino.common.table;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class CustomSer extends StdSerializer<UUID> implements JsonSerializable {
	public CustomSer() {
		super(UUID.class);
	}

	@Override
	public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
		System.out.println("t");
	}

	@Override
	public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("t");

	}

	@Override
	public void serialize(UUID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		
		gen.writeString(value.toString());
	}
}