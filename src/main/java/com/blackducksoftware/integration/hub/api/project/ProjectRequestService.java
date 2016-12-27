/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.api.project;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_PROJECTS;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.google.gson.JsonObject;

public class ProjectRequestService extends HubParameterizedRequestService<ProjectItem> {
    private static final List<String> PROJECTS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_PROJECTS);

    public ProjectRequestService(final RestConnection restConnection) {
        super(restConnection, ProjectItem.class);
    }

    public List<ProjectItem> getAllProjects() throws HubIntegrationException {
        final List<ProjectItem> allProjectItems = getAllItems(PROJECTS_SEGMENTS);
        return allProjectItems;
    }

    public List<ProjectItem> getAllProjectMatches(final String projectName) throws HubIntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.setQ("name:" + projectName);
        }

        final List<ProjectItem> allProjectItems = getAllItems(hubPagedRequest);
        return allProjectItems;
    }

    public List<ProjectItem> getProjectMatches(final String projectName, final int limit) throws HubIntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(limit, PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.setQ("name:" + projectName);
        }

        final List<ProjectItem> projectItems = getItems(hubPagedRequest);
        return projectItems;
    }

    public ProjectItem getProjectByName(String projectName) throws HubIntegrationException {
        final List<ProjectItem> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectItem project : allProjectItems) {
            if (projectName.equals(project.getName())) {
                return project;
            }
        }
        throw new HubIntegrationException("This Project does not exist. Project : " + projectName);
    }

    public String createHubProject(final String projectName) throws HubIntegrationException {
        final HubRequest projectItemRequest = getHubRequestFactory().createPostRequest(PROJECTS_SEGMENTS);
        JsonObject json = new JsonObject();
        json.addProperty("name", projectName);
        String location = projectItemRequest.executePost(getRestConnection().getGson().toJson(json));
        return location;
    }

}
