/**
 * Hub Common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

import okhttp3.Response;

public class ProjectRequestService extends HubResponseService {
    private static final List<String> PROJECTS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_PROJECTS);

    private final MetaService metaService;

    public ProjectRequestService(final RestConnection restConnection, final MetaService metaService) {
        super(restConnection);
        this.metaService = metaService;
    }

    public List<ProjectView> getAllProjects() throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(PROJECTS_SEGMENTS);
        final List<ProjectView> allProjectItems = getAllItems(hubPagedRequest, ProjectView.class);
        return allProjectItems;
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(100, PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.q = "name:" + projectName;
        }

        final List<ProjectView> allProjectItems = getAllItems(hubPagedRequest, ProjectView.class);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(limit, PROJECTS_SEGMENTS);
        if (StringUtils.isNotBlank(projectName)) {
            hubPagedRequest.q = "name:" + projectName;
        }

        final List<ProjectView> projectItems = getItems(hubPagedRequest, ProjectView.class);
        return projectItems;
    }

    public ProjectView getProjectByName(final String projectName) throws IntegrationException {
        final List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.name)) {
                return project;
            }
        }
        throw new DoesNotExistException("This Project does not exist. Project : " + projectName);
    }

    public String createHubProject(final ProjectRequest project) throws IntegrationException {
        final HubRequest projectItemRequest = getHubRequestFactory().createRequest(PROJECTS_SEGMENTS);
        Response response = null;
        try {
            final String projectJson = getGson().toJson(project);
            response = projectItemRequest.executePost(projectJson);
            return response.header("location");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void deleteHubProject(final ProjectView project) throws IntegrationException {
        final HubRequest deleteRequest = getHubRequestFactory().createRequest(metaService.getHref(project));
        deleteRequest.executeDelete();
    }

}
