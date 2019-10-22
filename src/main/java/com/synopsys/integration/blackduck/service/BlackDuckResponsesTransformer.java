/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckResponsesTransformer {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponsesTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllResponses(PagedRequest pagedRequest, Class<T> clazz) throws IntegrationException {
        return getResponses(pagedRequest, clazz, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(PagedRequest pagedRequest, Class<T> clazz, boolean getAll) throws IntegrationException {
        int limit = getAll ? Integer.MAX_VALUE : -1;
        return getResponses(pagedRequest, clazz, limit);
    }

    /**
     * @param maxLimit is the maximum number of entries returned. Providing a maxLimit of less than 0 will only get the first page of results.
     */
    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(PagedRequest pagedRequest, Class<T> clazz, int maxLimit) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        int pagedLimit = pagedRequest.getLimit();
        if (maxLimit >= 0) {
            pagedLimit = Integer.min(pagedLimit, maxLimit);
        }
        PagedRequest finalPagedRequest = new PagedRequest(pagedRequest.getRequestBuilder(), pagedRequest.getOffset(), pagedLimit);
        Request request = finalPagedRequest.createRequest();
        try (Response initialResponse = blackDuckHttpClient.execute(request)) {
            blackDuckHttpClient.throwExceptionForError(initialResponse);
            String initialJsonResponse = initialResponse.getContentString();
            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, clazz);
            allResponses.addAll(blackDuckPageResponse.getItems());

            totalCount = blackDuckPageResponse.getTotalCount();
            if (maxLimit < 0 || allResponses.size() == maxLimit) {
                return new BlackDuckPageResponse<>(totalCount, allResponses);
            }

            while (allResponses.size() < totalCount && allResponses.size() < maxLimit && currentOffset < totalCount) {
                currentOffset += finalPagedRequest.getLimit();
                int finalLimit = finalPagedRequest.getLimit();
                if (currentOffset + finalLimit > maxLimit) {
                    finalLimit = maxLimit - currentOffset;
                }
                PagedRequest offsetPagedRequest = new PagedRequest(finalPagedRequest.getRequestBuilder(), currentOffset, finalLimit);
                request = offsetPagedRequest.createRequest();
                try (Response response = blackDuckHttpClient.execute(request)) {
                    blackDuckHttpClient.throwExceptionForError(response);
                    String jsonResponse = response.getContentString();
                    blackDuckPageResponse = blackDuckJsonTransformer.getResponses(jsonResponse, clazz);
                    allResponses.addAll(blackDuckPageResponse.getItems());
                } catch (IOException e) {
                    throw new BlackDuckIntegrationException(e);
                }
            }
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }

        return new BlackDuckPageResponse<>(totalCount, allResponses);
    }

}
