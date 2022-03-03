/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckResponsesTransformer {
    Logger logger = LoggerFactory.getLogger(this.getClass()); //TODO- delete

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;

    public BlackDuckResponsesTransformer(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getSomeMatchingResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        return getInternalMatchingResponse(requestMultiple, totalLimit, predicate);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return getInternalMatchingResponse(requestMultiple, Integer.MAX_VALUE, alwaysTrue());
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getSomeResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, int totalLimit) throws IntegrationException {
        return getInternalMatchingResponse(requestMultiple, totalLimit, alwaysTrue());
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getOnePageOfResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return getInternalMatchingResponse(requestMultiple, getLimit(requestMultiple), alwaysTrue());
    }

    private <T extends BlackDuckResponse> Predicate<T> alwaysTrue() {
        return (blackDuckResponse) -> true;
    }

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> getInternalMatchingResponse(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, int maxToReturn, Predicate<T> predicate) throws IntegrationException {
        List<T> allResponses = new LinkedList<>();
        int totalCount = 0;

        int limit = getLimit(requestMultiple);
        int offset = getOffset(requestMultiple);
        try (Response initialResponse = blackDuckHttpClient.execute(requestMultiple)) {
            blackDuckHttpClient.throwExceptionForError(initialResponse);
            String initialJsonResponse = initialResponse.getContentString();

            logger.info(String.format("Initial response: %s", initialJsonResponse)); //TODO- delete

            BlackDuckPageResponse<T> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(initialJsonResponse, requestMultiple.getResponseClass());

            allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate));

            totalCount = blackDuckPageResponse.getTotalCount();
            int totalItemsToRetrieve = Math.min(totalCount, maxToReturn);

            while (allResponses.size() < totalItemsToRetrieve && offset < totalCount) {
                offset = offset + limit;
                requestMultiple = nextPage(requestMultiple, offset);
                try (Response response = blackDuckHttpClient.execute(requestMultiple)) {
                    blackDuckHttpClient.throwExceptionForError(response);
                    String jsonResponse = response.getContentString();
                    blackDuckPageResponse = blackDuckJsonTransformer.getResponses(jsonResponse, requestMultiple.getResponseClass());
                    allResponses.addAll(this.matchPredicate(blackDuckPageResponse, predicate));
                } catch (IOException e) {
                    throw new BlackDuckIntegrationException(e);
                }
            }

            allResponses = onlyReturnMaxRequested(maxToReturn, allResponses);

            logger.info(String.format("Final responses: %s", allResponses)); //TODO- delete

            return new BlackDuckPageResponse<>(totalCount, allResponses);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> BlackDuckRequest<T, UrlMultipleResponses<T>> nextPage(BlackDuckRequest<T, UrlMultipleResponses<T>> blackDuckRequest, int offset) {
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder(blackDuckRequest);
        blackDuckRequestBuilder.setOffset(offset);

        return new BlackDuckRequest<>(blackDuckRequestBuilder, blackDuckRequest.getUrlResponse());
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

    public int getLimit(BlackDuckRequest<?, ?> blackDuckRequest) {
        return retrieveValue(blackDuckRequest.getRequest().getQueryParameters()::get, BlackDuckRequestBuilder.LIMIT_PARAMETER, BlackDuckRequestBuilder.DEFAULT_LIMIT);
    }

    public int getOffset(BlackDuckRequest<?, ?> blackDuckRequest) {
        return retrieveValue(blackDuckRequest.getRequest().getQueryParameters()::get, BlackDuckRequestBuilder.OFFSET_PARAMETER, BlackDuckRequestBuilder.DEFAULT_OFFSET);
    }

    private int retrieveValue(Function<String, Set<String>> valueCollection, String key, int defaultValue) {
        return NumberUtils.toInt(valueCollection.apply(key).stream().findFirst().orElse(Integer.toString(defaultValue)));
    }

}
