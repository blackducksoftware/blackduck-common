package com.blackducksoftware.integration.hub.dataservices;

import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public abstract class AbstractDataService {
	private final RestConnection restConnection;
	private final Gson gson;
	private final JsonParser jsonParser;

	public AbstractDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		this.restConnection = restConnection;
		this.gson = gson;
		this.jsonParser = jsonParser;
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public Gson getGson() {
		return gson;
	}

	public JsonParser getJsonParser() {
		return jsonParser;
	}
}
