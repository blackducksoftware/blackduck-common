package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ProjectVersionRestService extends HubRestService<ReleaseItem> {
	public ProjectVersionRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<ReleaseItem>() {
		}.getType(), new TypeToken<List<ReleaseItem>>() {
		}.getType());
	}

	public ReleaseItem getProjectVersionReleaseItem(final String versionUrl)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(versionUrl);
	}

}
