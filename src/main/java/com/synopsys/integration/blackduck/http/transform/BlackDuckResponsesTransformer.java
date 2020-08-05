/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.http.transform;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckResponsesTransformer {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponsesTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    // Will it only be getAllResponses that uses an actual Predicate and the other 2 (getSomeResponses, getOnePageOfResponses) will use some kind of default or null?
    // Will getAllResponses (and the other 2) be overloaded to accept a Predicate AND to use a default or null?
    // The Predicate param will be Predicate<T>, yes?
    // Would we do some sort of check on the Predicate to see if we need to run it?
    // So the Predicate found a match. Now what? We want to stop and return, but what do we return? Just the List with that single entry, right?
    // Will the Predicate passed in by CodeLocationService.getCodeLocationByName be something like this:
    //          codeLocationView -> codeLocationName.equalsIgnoreCase(codeLocationView.getName())
    // I assume we want one method for getInternalResponses. If so, how do we handle things like the test using currentOffset && totalItemsToRetrieve?
    //          If totalItemsToRetrieve is 1, as soon as we go to the second page, currentOffset will be greater than totalItemsToRetrieve.

    // Testing: Would the verification be to ensure that allPagesResponse.getItems().size() is 1 for a found match and 0 for no match?
    //      The match would be based on the Predicate working correctly.
    //      Also on a match, verifying we got the correct match of course.

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getMatchingResponse(PagedRequest pagedRequest, Class<T> clazz, Predicate<T> predicate) throws IntegrationException {
        return getInternalMatchingResponse(pagedRequest, clazz, 1, predicate);
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

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> getInternalResponses(PagedRequest pagedRequest, Class<T> clazz, int totalLimit) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
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

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> getInternalMatchingResponse(PagedRequest pagedRequest, Class<T> clazz, int totalLimit, Predicate<T> predicate) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        Request request = pagedRequest.createRequest();
        try (Response initialResponse = blackDuckHttpClient.execute(request)) {
            blackDuckHttpClient.throwExceptionForError(initialResponse);
            String initialJsonResponse = initialResponse.getContentString();
            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, clazz);

            allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate)); // <<<<-----HERE

            totalCount = blackDuckPageResponse.getTotalCount();
            // int totalItemsToRetrieve = Math.min(totalCount, totalLimit); // <<<<-----HERE

            //while (allResponses.size() < totalItemsToRetrieve && currentOffset < totalItemsToRetrieve) { // <<<<-----HERE
            while (allResponses.size() < totalCount && allResponses.size() < totalLimit) { // <<<<-----HERE
                currentOffset += pagedRequest.getLimit();
                PagedRequest offsetPagedRequest = new PagedRequest(pagedRequest.getRequestBuilder(), currentOffset, pagedRequest.getLimit());
                request = offsetPagedRequest.createRequest();
                try (Response response = blackDuckHttpClient.execute(request)) {
                    blackDuckHttpClient.throwExceptionForError(response);
                    String jsonResponse = response.getContentString();
                    blackDuckPageResponse = blackDuckJsonTransformer.getResponses(jsonResponse, clazz);
                    allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate)); // <<<<-----HERE
                } catch (IOException e) {
                    throw new BlackDuckIntegrationException(e);
                }
            }

            allResponses = allResponses.stream().limit(totalLimit).collect(Collectors.toList());  // ???? Why do you do this line?
            return new BlackDuckPageResponse<>(totalCount, allResponses);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> List<T> matchPredicate(BlackDuckPageResponse<T> blackDuckPageResponse, Predicate<T> predicate) {
        if (predicate == null) {
            return blackDuckPageResponse.getItems();
        } else {
            return blackDuckPageResponse
                       .getItems()
                       .stream()
                       .filter(predicate)
                       .collect(Collectors.toList());
        }
    }

}
