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
	public static final Type TYPE_TOKEN_ITEM = new TypeToken<ProjectItem>() {
	}.getType();
	public static final Type TYPE_TOKEN_LIST = new TypeToken<List<ProjectItem>>() {
	}.getType();

	private final List<String> getProjectsSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_PROJECTS);

	public ProjectRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public List<ProjectItem> getAllProjects() throws IOException, BDRestException, URISyntaxException {
		final HubRequest projectItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectItemRequest.setMethod(Method.GET);
		projectItemRequest.setLimit(100);
		projectItemRequest.addUrlSegments(getProjectsSegments);

		final JsonObject jsonObject = projectItemRequest.executeForResponseJson();
		final List<ProjectItem> allProjectItems = getAll(jsonObject, projectItemRequest);
		return allProjectItems;
	}

	public List<ProjectItem> getAllProjectMatches(final String projectName)
			throws IOException, BDRestException, URISyntaxException {
		final HubRequest projectItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectItemRequest.setMethod(Method.GET);
		projectItemRequest.setLimit(100);
		projectItemRequest.addUrlSegments(getProjectsSegments);
		if (StringUtils.isNotBlank(projectName)) {
			projectItemRequest.setQ("name:" + projectName);
		}

		final JsonObject jsonObject = projectItemRequest.executeForResponseJson();
		final List<ProjectItem> allProjectItems = getAll(jsonObject, projectItemRequest);
		return allProjectItems;
	}

	public List<ProjectItem> getProjectMatches(final String projectName, final int limit)
			throws IOException, BDRestException, URISyntaxException {
		final HubRequest projectItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		projectItemRequest.setMethod(Method.GET);
		projectItemRequest.setLimit(limit);
		projectItemRequest.addUrlSegments(getProjectsSegments);
		if (StringUtils.isNotBlank(projectName)) {
			projectItemRequest.setQ("name:" + projectName);
		}

		final JsonObject jsonObject = projectItemRequest.executeForResponseJson();
		final List<ProjectItem> allProjectItems = getItems(jsonObject);
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
		return getItem(projectUrl);
	}

}
