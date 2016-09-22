package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

	public ReleaseItem getProjectVersionByName(final String projectId, final String projectVersionName)
			throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException,
			HubIntegrationException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add(UrlConstants.SEGMENT_API);
		urlSegments.add(UrlConstants.SEGMENT_PROJECTS);
		urlSegments.add(projectId);
		urlSegments.add(UrlConstants.SEGMENT_VERSIONS);

		final HubRequest projectVersionItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectVersionItemRequest.setMethod(Method.GET);
		projectVersionItemRequest.setLimit(5);
		projectVersionItemRequest.addUrlSegments(urlSegments);

		final JsonObject jsonObject = projectVersionItemRequest.executeForResponseJson();
		final List<ReleaseItem> allReleaseItems = getItems(jsonObject);
		for (final ReleaseItem releaseItem : allReleaseItems) {
			if (projectVersionName.equals(releaseItem.getVersionName())) {
				return releaseItem;
			}
		}

		throw new HubIntegrationException(
				String.format("The version %s does not exist for projecId %s.", projectVersionName, projectId));
	}

}
