package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;

import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ProjectVersionRestService extends HubRestService<ReleaseItem> {
	private final Type releaseItemType = new TypeToken<ReleaseItem>() {
	}.getType();

	public ProjectVersionRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
	}

	public ReleaseItem getProjectVersionReleaseItem(final String versionUrl)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(releaseItemType, versionUrl);
	}
}
