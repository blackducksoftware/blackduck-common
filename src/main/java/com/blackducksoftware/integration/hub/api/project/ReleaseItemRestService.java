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
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_VERSIONS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ReleaseItemRestService extends HubItemRestService<ReleaseItem> {
    private static final Type ITEM_TYPE = new TypeToken<ReleaseItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<ReleaseItem>>() {
    }.getType();

    public ReleaseItemRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public ReleaseItem getProjectVersionByName(final String projectId, final String projectVersionName)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException,
            HubIntegrationException {
        final List<String> urlSegments = new ArrayList<>();
        urlSegments.add(SEGMENT_API);
        urlSegments.add(SEGMENT_PROJECTS);
        urlSegments.add(projectId);
        urlSegments.add(SEGMENT_VERSIONS);

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

    @Deprecated
    public String createHubVersion(final ProjectItem project, final String versionName, final String phase,
            final String dist) throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
        final ReleaseItem newRelease = new ReleaseItem(versionName, phase, dist, null, null);

        final StringRepresentation stringRep = new StringRepresentation(getGson().toJson(newRelease));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        stringRep.setCharacterSet(CharacterSet.UTF_8);

        final HubRequest projectVersionItemRequest = new HubRequest(getRestConnection(), getJsonParser());
        projectVersionItemRequest.setMethod(Method.POST);
        projectVersionItemRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        projectVersionItemRequest.setOffset(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        projectVersionItemRequest.setUrl(project.getLink(ProjectItem.VERSION_LINK));

        String location = null;
        try {
            location = projectVersionItemRequest.executePost(stringRep);
        } catch (final ResourceDoesNotExistException ex) {
            throw new BDRestException("There was a problem creating this Version for the specified Hub Project. ", ex,
                    ex.getResource());
        }

        return location;
    }

}
