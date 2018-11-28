/**
 * blackduck-common
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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckResponsesTransformer {
    private final BlackDuckRestConnection restConnection;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponsesTransformer(final BlackDuckRestConnection restConnection, final BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.restConnection = restConnection;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllResponses(final PagedRequest pagedRequest, final Class<T> clazz) throws IntegrationException {
        return getResponses(pagedRequest, clazz, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz, final boolean getAll) throws IntegrationException {
        final List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        Request request = pagedRequest.createRequest();
        try (final Response initialResponse = restConnection.execute(request)) {
            initialResponse.throwExceptionForError();
            final String initialJsonResponse = initialResponse.getContentString();
            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, clazz);
            allResponses.addAll(blackDuckPageResponse.getItems());

            totalCount = blackDuckPageResponse.getTotalCount();
            if (!getAll) {
                return new BlackDuckPageResponse<>(totalCount, allResponses);
            }

            while (allResponses.size() < totalCount && currentOffset < totalCount) {
                currentOffset += pagedRequest.getLimit();
                final PagedRequest offsetPagedRequest = new PagedRequest(pagedRequest.getRequestBuilder(), currentOffset, pagedRequest.getLimit());
                request = offsetPagedRequest.createRequest();
                try (final Response response = restConnection.execute(request)) {
                    response.throwExceptionForError();
                    final String jsonResponse = response.getContentString();
                    blackDuckPageResponse = blackDuckJsonTransformer.getResponses(jsonResponse, clazz);
                    allResponses.addAll(blackDuckPageResponse.getItems());
                } catch (final IOException e) {
                    throw new BlackDuckIntegrationException(e);
                }
            }
        } catch (final IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }

        return new BlackDuckPageResponse<>(totalCount, allResponses);
    }

}
