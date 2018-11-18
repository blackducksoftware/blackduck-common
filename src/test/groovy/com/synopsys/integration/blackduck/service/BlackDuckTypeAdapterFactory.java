package com.synopsys.integration.blackduck.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;

public class BlackDuckTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        final Class<T> rawType = (Class<T>) type.getRawType();
        if (!BlackDuckResponse.class.isAssignableFrom(rawType)) {
            return null;
        }

        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override
            public void write(final JsonWriter out, final T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(final JsonReader reader) throws IOException {
                return delegate.read(reader);
            }
        };
    }
}
