package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
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
	public ComponentVersionRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<ComponentVersion>() {
		}.getType(), new TypeToken<List<ComponentVersion>>() {
		}.getType());
	}

	public ComponentVersion getComponentVersion(final String componentVersionUrl)
			throws NotificationServiceException, IOException, BDRestException, URISyntaxException {
		return getItem(componentVersionUrl);
	}

}
