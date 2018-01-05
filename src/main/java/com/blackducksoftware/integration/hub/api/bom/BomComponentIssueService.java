/**
 * hub-common
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
package com.blackducksoftware.integration.hub.api.bom;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.IssueView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class BomComponentIssueService extends HubService {
    public BomComponentIssueService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String createIssue(final IssueView issueItem, final String url) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        Response response = null;
        try {
            response = request.executePost(getGson().toJson(issueItem));
            return response.header("location");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void updateIssue(final IssueView issueItem, final String url) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        Response response = null;
        try {
            response = request.executePut(getGson().toJson(issueItem));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void deleteIssue(final IssueView issueItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(issueItem);
        deleteIssue(codeLocationItemUrl);
    }

    public void deleteIssue(final String issueItemUrl) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(issueItemUrl);
        request.executeDelete();
    }

}
