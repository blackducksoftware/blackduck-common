/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.transform.adapters;

import com.blackduck.integration.rest.RestConstants;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
