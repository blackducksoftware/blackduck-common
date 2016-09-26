package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.ComponentVersion;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ComponentVersionRestService extends HubRestService<ComponentVersion> {
	public static final Type TYPE_TOKEN_ITEM = new TypeToken<ComponentVersion>() {
	}.getType();
	public static final Type TYPE_TOKEN_LIST = new TypeToken<List<ComponentVersion>>() {
	}.getType();

	public ComponentVersionRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public ComponentVersion getComponentVersion(final String componentVersionUrl)
			throws NotificationServiceException, IOException, BDRestException, URISyntaxException {
		return getItem(componentVersionUrl);
	}

}
