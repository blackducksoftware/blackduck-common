/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.project;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_PROJECTS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubPagedRequest;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.api.project.version.SourceEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.reflect.TypeToken;

public class ProjectRestService extends HubItemRestService<ProjectItem> {
    private static final List<String> PROJECTS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_PROJECTS);

    private static final Type ITEM_TYPE = new TypeToken<ProjectItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<ProjectItem>>() {
    }.getType();

    public ProjectRestService(final RestConnection restConnection) {
        super(restConnection, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public List<ProjectItem> getAllProjects() throws IOException, BDRestException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(100, PROJECTS_SEGMENTS);

        final List<ProjectItem> allProjectItems = getAllHubItems(hubPagedRequest);
        return allProjectItems;
    }

    public List<ProjectItem> getAllProjectMatches(final String projectName)
            throws IOException, BDRestException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(100, PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.setQ("name:" + projectName);
        }

        final List<ProjectItem> allProjectItems = getAllHubItems(hubPagedRequest);
        return allProjectItems;
    }

    public List<ProjectItem> getProjectMatches(final String projectName, final int limit)
            throws IOException, BDRestException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(limit, PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.setQ("name:" + projectName);
        }

        final List<ProjectItem> allProjectItems = getHubItems(hubPagedRequest).getItems();
        return allProjectItems;
    }

    public ProjectItem getProjectByName(String projectName)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException {
        final List<ProjectItem> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectItem project : allProjectItems) {
            if (projectName.equals(project.getName())) {
                return project;
            }
        }
        throw new ProjectDoesNotExistException("This Project does not exist. Project : " + projectName);
    }

    public String createHubProject(final String projectName) throws IOException, BDRestException, URISyntaxException {
        final ProjectItem newProject = new ProjectItem(null, projectName, null, false, 1, SourceEnum.CUSTOM);
        final StringRepresentation stringRepresentation = new StringRepresentation(getRestConnection().getGson().toJson(newProject));
        stringRepresentation.setMediaType(MediaType.APPLICATION_JSON);
        stringRepresentation.setCharacterSet(CharacterSet.UTF_8);

        HubRequest projectItemRequest = getHubRequestFactory().createPostRequest(PROJECTS_SEGMENTS);
        String location = null;
        try {
            location = projectItemRequest.executePost(stringRepresentation);
        } catch (final ResourceDoesNotExistException ex) {
            throw new BDRestException("There was a problem creating this Project for the specified Hub server.", ex,
                    ex.getResource());
        }
        return location;
    }

}
