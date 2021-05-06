/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.transform;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.blackduck.service.request.PagingEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckResponsesTransformer {
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final PagingEditor pagingEditor = new PagingEditor();

    public BlackDuckResponsesTransformer(BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getSomeMatchingResponses(BlackDuckRequest<T> blackDuckRequest, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        return getInternalMatchingResponse(blackDuckRequest, totalLimit, predicate);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllResponses(BlackDuckRequest<T> blackDuckRequest) throws IntegrationException {
        return getInternalMatchingResponse(blackDuckRequest, Integer.MAX_VALUE, alwaysTrue());
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getSomeResponses(BlackDuckRequest<T> blackDuckRequest, int totalLimit) throws IntegrationException {
        return getInternalMatchingResponse(blackDuckRequest, totalLimit, alwaysTrue());
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getOnePageOfResponses(BlackDuckRequest<T> blackDuckRequest) throws IntegrationException {
        return getInternalMatchingResponse(blackDuckRequest, getLimit(blackDuckRequest.getRequest()), alwaysTrue());
    }

    private <T extends BlackDuckResponse> Predicate<T> alwaysTrue() {
        return (blackDuckResponse) -> true;
    }

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> getInternalMatchingResponse(BlackDuckRequest<T> blackDuckRequest, int maxToReturn, Predicate<T> predicate) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        Request request = blackDuckRequest.getRequest(pagingEditor);
        int limit = getLimit(request);
        int offset = getOffset(request);
        try (Response initialResponse = blackDuckHttpClient.execute(request)) {
            blackDuckHttpClient.throwExceptionForError(initialResponse);
            String initialJsonResponse = initialResponse.getContentString();
            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, blackDuckRequest.getResponseClass());

            allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate));

            totalCount = blackDuckPageResponse.getTotalCount();
            int totalItemsToRetrieve = Math.min(totalCount, maxToReturn);

            while (allResponses.size() < totalItemsToRetrieve && offset < totalCount) {
                offset = offset + limit;
                blackDuckRequest = nextPage(blackDuckRequest, offset);
                request = blackDuckRequest.getRequest();
                try (Response response = blackDuckHttpClient.execute(request)) {
                    blackDuckHttpClient.throwExceptionForError(response);
                    String jsonResponse = response.getContentString();
                    blackDuckPageResponse = blackDuckJsonTransformer.getResponses(jsonResponse, blackDuckRequest.getResponseClass());
                    allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate));
                } catch (IOException e) {
                    throw new BlackDuckIntegrationException(e);
                }
            }

            allResponses = onlyReturnMaxRequested(maxToReturn, allResponses);
            return new BlackDuckPageResponse<>(totalCount, allResponses);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> BlackDuckRequest<T> nextPage(BlackDuckRequest<T> blackDuckRequest, int offset) {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createBlackDuckRequestBuilder(blackDuckRequest.getRequest());
        blackDuckRequestBuilder.setOffset(offset);

        return new BlackDuckRequest<>(blackDuckRequestBuilder, blackDuckRequest.getResponseClass());
    }

    @NotNull
    private <T extends BlackDuckResponse> List<T> onlyReturnMaxRequested(int maxToReturn, List<T> allResponses) {
        return allResponses.stream().limit(maxToReturn).collect(Collectors.toList());
    }

    private <T extends BlackDuckResponse> List<T> matchPredicate(BlackDuckPageResponse<T> blackDuckPageResponse, Predicate<T> predicate) {
        return blackDuckPageResponse
                   .getItems()
                   .stream()
                   .filter(predicate)
                   .collect(Collectors.toList());
    }

    public int getLimit(Request request) {
        return retrieveValue(request.getQueryParameters()::get, BlackDuckRequestBuilder.LIMIT_PARAMETER, BlackDuckRequestBuilder.DEFAULT_LIMIT);
    }

    public int getOffset(Request request) {
        return retrieveValue(request.getQueryParameters()::get, BlackDuckRequestBuilder.OFFSET_PARAMETER, BlackDuckRequestBuilder.DEFAULT_OFFSET);
    }

    private int retrieveValue(Function<String, Set<String>> valueCollection, String key, int defaultValue) {
        return NumberUtils.toInt(valueCollection.apply(key).stream().findFirst().orElse(Integer.toString(defaultValue)));
    }

}
