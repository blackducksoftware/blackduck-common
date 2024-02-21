/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.transform.adapters;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.synopsys.integration.rest.RestConstants;

public class OffsetDateTimeTypeAdapter extends TypeAdapter<OffsetDateTime> implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    private final DateTimeFormatter formatter;

    public OffsetDateTimeTypeAdapter() {
        this.formatter = DateTimeFormatter.ofPattern(RestConstants.JSON_DATE_FORMAT).withZone(ZoneOffset.UTC);
    }

    @Override
    public void write(final JsonWriter out, final OffsetDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        String formattedTime = formatter.format(value);
        out.value(formattedTime);
    }

    @Override
    public OffsetDateTime read(final JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String timestampString = in.nextString();
        return formatter.parse(timestampString,OffsetDateTime::from);
    }

    @Override
    public OffsetDateTime deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        return formatter.parse(json.getAsString(), OffsetDateTime::from);
    }

    @Override
    public JsonElement serialize(final OffsetDateTime src, final Type typeOfSrc, final JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
}
