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
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckResponsesTransformer {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final MediaTypeDiscovery mediaTypeDiscovery;

    public BlackDuckResponsesTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer, MediaTypeDiscovery mediaTypeDiscovery) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.mediaTypeDiscovery = mediaTypeDiscovery;
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllResponses(PagedRequest pagedRequest, Class<T> clazz) throws IntegrationException {
        return getInternalResponses(pagedRequest, clazz, Integer.MAX_VALUE);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getSomeResponses(PagedRequest pagedRequest, Class<T> clazz, int totalLimit) throws IntegrationException {
        return getInternalResponses(pagedRequest, clazz, totalLimit);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getOnePageOfResponses(PagedRequest pagedRequest, Class<T> clazz) throws IntegrationException {
        return getInternalResponses(pagedRequest, clazz, pagedRequest.getLimit());
    }

    @Deprecated
    /**
     * @deprecated Please use the appropriate getAll or getSome method
     */
    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(PagedRequest pagedRequest, Class<T> clazz, boolean getAll) throws IntegrationException {
        int totalLimit = Integer.MAX_VALUE;
        if (!getAll) {
            totalLimit = pagedRequest.getLimit();
        }
        return getInternalResponses(pagedRequest, clazz, totalLimit);
    }

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> getInternalResponses(PagedRequest pagedRequest, Class<T> clazz, int totalLimit) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        applyMediaType(pagedRequest.getRequestBuilder(), clazz);
        Request request = pagedRequest.createRequest();
        try (Response initialResponse = blackDuckHttpClient.execute(request)) {
            blackDuckHttpClient.throwExceptionForError(initialResponse);
            String initialJsonResponse = initialResponse.getContentString();
            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, clazz);
            allResponses.addAll(blackDuckPageResponse.getItems());

            totalCount = blackDuckPageResponse.getTotalCount();
            int totalItemsToRetrieve = Math.min(totalCount, totalLimit);

            while (allResponses.size() < totalItemsToRetrieve && currentOffset < totalItemsToRetrieve) {
                currentOffset += pagedRequest.getLimit();
                PagedRequest offsetPagedRequest = new PagedRequest(pagedRequest.getRequestBuilder(), currentOffset, pagedRequest.getLimit());
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

            allResponses = allResponses.stream().limit(totalLimit).collect(Collectors.toList());
            return new BlackDuckPageResponse<>(totalCount, allResponses);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> void applyMediaType(Request.Builder requestBuilder, Class<T> clazz) {
        Optional<String> mediaType = mediaTypeDiscovery.determineMediaType(clazz);
        if (mediaType.isPresent()) {
            requestBuilder.addAdditionalHeader("Accept", mediaType.get());
        }
    }
}
