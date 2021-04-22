/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.request.BlackDuckApiExchangeDescriptorFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckApiExchangeDescriptorMultiple;
import com.synopsys.integration.blackduck.service.request.BlackDuckApiExchangeDescriptorSingle;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingBiFunction;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckApiClient {
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import");
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries");
    public static final BlackDuckPath UPLOADS_PATH = new BlackDuckPath("/api/uploads");
    public static final BlackDuckPath SCAN_DATA_PATH = new BlackDuckPath("/api/scan/data/"); // TODO: Determine why this endpoint requires a slash at the end.
    public static final BlackDuckPath SCAN_DEVELOPER_MODE_PATH = new BlackDuckPath("/api/developer-scans");
    public static final BlackDuckPath SCAN_INTELLIGENT_PERSISTENCE_MODE_PATH = new BlackDuckPath("/api/intelligent-persistence-scans");

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;
    private final BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory;

    public BlackDuckApiClient(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
        BlackDuckResponsesTransformer blackDuckResponsesTransformer, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, BlackDuckApiExchangeDescriptorFactory blackDuckApiExchangeDescriptorFactory) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.blackDuckResponseTransformer = blackDuckResponseTransformer;
        this.blackDuckResponsesTransformer = blackDuckResponsesTransformer;
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
        this.blackDuckApiExchangeDescriptorFactory = blackDuckApiExchangeDescriptorFactory;
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createCommonGetRequestBuilder();
        return getSomeMatchingResponses(blackDuckPathMultipleResponses, blackDuckRequestBuilder, predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, predicate, totalLimit);
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckPath(blackDuckPathMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckPathMultipleResponses, createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = blackDuckResponsesTransformer::getAllResponses;
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckPath(blackDuckPathMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckPathMultipleResponses, createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder, int totalLimit) throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit);
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckPath(blackDuckPathMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        BlackDuckPath blackDuckPath = blackDuckPathMultipleResponses.getBlackDuckPath();
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createBlackDuckRequestBuilder(blackDuckPath);
        Class<T> responseClass = blackDuckPathMultipleResponses.getResponseClass();
        return getPageResponse(blackDuckRequestBuilder, responseClass, blackDuckPageDefinition);
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        HttpUrl url = blackDuckPathSingleResponse.getBlackDuckPath().getFullBlackDuckUrl(blackDuckBaseUrl);
        requestBuilder.url(url);
        return blackDuckResponseTransformer.getResponse(requestBuilder.build(), blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        return getResponse(blackDuckPathSingleResponse, createCommonGetRequestBuilder());
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckView
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getSomeMatchingResponses(blackDuckView, linkMultipleResponses, createCommonGetRequestBuilder(), predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, predicate, totalLimit);
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckView(blackDuckView, linkMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckView, linkMultipleResponses, createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = blackDuckResponsesTransformer::getAllResponses;
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckView(blackDuckView, linkMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckView, linkMultipleResponses, createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder, int totalLimit) throws IntegrationException {
        ResponsesTransformer<T> responsesTransformer = (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit);
        BlackDuckApiExchangeDescriptorMultiple<T> descriptorMultiple = blackDuckApiExchangeDescriptorFactory.fromBlackDuckView(blackDuckView, linkMultipleResponses, requestBuilder);
        TransformerDetails<T> transformerDetails = new TransformerDetails<>(descriptorMultiple, responsesTransformer);
        return getSpecialResponses(transformerDetails);
    }

    public <T extends BlackDuckResponse> Optional<T> getResponse(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        try {
            HttpUrl url = blackDuckView.getFirstLink(linkSingleResponse.getLink());
            Request request = blackDuckRequestBuilderFactory.createCommonGetRequest(url);
            return Optional.of(blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass()));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(HttpUrl url, Class<T> responseClass, int totalLimit) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(HttpUrl url, Class<T> responseClass, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(url);
        return getPageResponse(requestBuilder, responseClass, blackDuckPageDefinition);
    }

    // ------------------------------------------------
    // getting responses from a Request.Builder
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass) throws IntegrationException {
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        PagedRequest pagedRequest = new PagedRequest(requestBuilder, blackDuckPageDefinition);
        return blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, responseClass);
    }

    // ------------------------------------------------
    // handling descriptors
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckApiExchangeDescriptorMultiple<T> blackDuckApiExchangeDescriptorMultiple) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckApiExchangeDescriptorMultiple.getBlackDuckRequestBuilder();
        PagedRequest pagedRequest = new PagedRequest(blackDuckRequestBuilder);

        Class<T> responseClass = blackDuckApiExchangeDescriptorMultiple.getResponseClass();

        return blackDuckResponsesTransformer.getAllResponses(pagedRequest, responseClass).getItems();
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckApiExchangeDescriptorSingle<T> blackDuckApiExchangeDescriptorSingle) throws IntegrationException {
        Request request = blackDuckApiExchangeDescriptorSingle.getBlackDuckRequestBuilder().build();

        Class<T> responseClass = blackDuckApiExchangeDescriptorSingle.getResponseClass();

        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(BlackDuckView blackDuckView) throws IntegrationException {
        HttpUrl url = blackDuckView.getHref();
        delete(url);
    }

    public void delete(HttpUrl url) throws IntegrationException {
        Request request = new Request.Builder(url, HttpMethod.DELETE).build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic put
    // ------------------------------------------------
    public void put(BlackDuckView blackDuckView) throws IntegrationException {
        HttpUrl url = blackDuckView.getHref();
        // add the 'missing' pieces back from view that could have been lost
        String json = blackDuckJsonTransformer.producePatchedJson(blackDuckView);
        Request request = blackDuckRequestBuilderFactory.createCommonPutRequestBuilder(url, json).build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(HttpUrl url) throws IntegrationException {
        Request request = blackDuckRequestBuilderFactory.createCommonGetRequest(url);
        return execute(request);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response execute(Request request) throws IntegrationException {
        Response response = blackDuckHttpClient.execute(request);
        blackDuckHttpClient.throwExceptionForError(response);
        return response;
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public HttpUrl executePostRequestAndRetrieveURL(Request request) throws IntegrationException {
        try (Response response = execute(request)) {
            return new HttpUrl(response.getHeaderValue("location"));
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private BlackDuckRequestBuilder createCommonGetRequestBuilder() {
        return blackDuckRequestBuilderFactory
                   .createBlackDuckRequestBuilder()
                   .commonGet();
    }

    private <T extends BlackDuckResponse> List<T> getSpecialResponses(TransformerDetails<T> transformerDetails) throws IntegrationException {
        Class<T> responseClass = transformerDetails.getResponseClass();
        return transformerDetails.responsesTransformer.apply(new PagedRequest(transformerDetails.getBlackDuckRequestBuilder()), responseClass).getItems();
    }

    private interface ResponsesTransformer<T extends BlackDuckResponse> extends ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> {}

    private static class TransformerDetails<T extends BlackDuckResponse> {
        public final BlackDuckApiExchangeDescriptorMultiple<T> blackDuckApiExchangeDescriptorMultiple;
        public final ResponsesTransformer<T> responsesTransformer;

        public TransformerDetails(BlackDuckApiExchangeDescriptorMultiple<T> blackDuckApiExchangeDescriptorMultiple, ResponsesTransformer<T> responsesTransformer) {
            this.blackDuckApiExchangeDescriptorMultiple = blackDuckApiExchangeDescriptorMultiple;
            this.responsesTransformer = responsesTransformer;
        }

        public Class<T> getResponseClass() {
            return blackDuckApiExchangeDescriptorMultiple.getResponseClass();
        }

        public BlackDuckRequestBuilder getBlackDuckRequestBuilder() {
            return blackDuckApiExchangeDescriptorMultiple.getBlackDuckRequestBuilder();
        }
    }

}
