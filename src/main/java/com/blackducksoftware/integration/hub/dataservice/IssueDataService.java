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
package com.blackducksoftware.integration.hub.dataservice;

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UpdateRequestWrapper;
import com.blackducksoftware.integration.hub.service.HubService;

public class IssueDataService extends HubService {

    public IssueDataService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String createIssue(final IssueView issueItem, final String uri) throws IntegrationException {
        final UpdateRequestWrapper requestWrapper = new UpdateRequestWrapper(HttpMethod.POST, issueItem);
        return executePostRequestAndRetrieveURL(uri, requestWrapper);
    }

    public void updateIssue(final IssueView issueItem, final String uri) throws IntegrationException {
        final UpdateRequestWrapper requestWrapper = new UpdateRequestWrapper(HttpMethod.PUT, issueItem);
        try (Response response = executeUpdateRequest(uri, requestWrapper)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteIssue(final IssueView issueItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(issueItem);
        deleteIssue(codeLocationItemUrl);
    }

    public void deleteIssue(final String issueItemUri) throws IntegrationException {
        try (Response response = executeUpdateRequest(issueItemUri, new UpdateRequestWrapper(HttpMethod.DELETE))) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
