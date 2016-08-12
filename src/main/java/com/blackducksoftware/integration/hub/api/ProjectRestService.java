package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ProjectRestService extends HubRestService<ProjectItem> {
	private final List<String> getProjectsSegments = Arrays.asList("api", "projects");
	private final Type projectItemListType = new TypeToken<List<ProjectItem>>() {
	}.getType();
	private final Type projectItemType = new TypeToken<ProjectItem>() {
	}.getType();

	public ProjectRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
	}

	public List<ProjectItem> getAllProjectMatches(final String projectName)
			throws IOException, BDRestException, URISyntaxException {
		final HubRequest projectItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectItemRequest.setMethod(Method.GET);
		projectItemRequest.addUrlSegments(getProjectsSegments);
		if (StringUtils.isNotBlank(projectName)) {
			projectItemRequest.addQueryParameter("q", "name:" + projectName);
		}

		final JsonObject jsonObject = projectItemRequest.executeForResponseJson();
		final List<ProjectItem> allProjectItems = getAll(projectItemListType, jsonObject, projectItemRequest);
		return allProjectItems;
	}

	public List<ProjectItem> getProjectMatches(final String projectName, final int limit)
			throws IOException, BDRestException, URISyntaxException {
		final HubRequest projectItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectItemRequest.setMethod(Method.GET);
		projectItemRequest.addUrlSegments(getProjectsSegments);
		if (StringUtils.isNotBlank(projectName)) {
			projectItemRequest.addQueryParameter("q", "name:" + projectName);
		}
		if (limit > 0) {
			projectItemRequest.addQueryParameter("limit", Integer.toString(limit));
		}

		final JsonObject jsonObject = projectItemRequest.executeForResponseJson();
		final List<ProjectItem> allProjectItems = getItems(projectItemListType, jsonObject);
		return allProjectItems;
	}

	public ProjectItem getProjectByName(String projectName)
			throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException {
		projectName = StringUtils.trimToEmpty(projectName);
		final List<ProjectItem> allProjectItems = getAllProjectMatches(projectName);
		for (final ProjectItem project : allProjectItems) {
			if (projectName.equals(project.getName())) {
				return project;
			}
		}
		throw new ProjectDoesNotExistException("This Project does not exist. Project : " + projectName);
	}

	public ProjectItem getProject(final String projectUrl) throws IOException, BDRestException, URISyntaxException {
		return getItem(projectItemType, projectUrl);
	}
}
