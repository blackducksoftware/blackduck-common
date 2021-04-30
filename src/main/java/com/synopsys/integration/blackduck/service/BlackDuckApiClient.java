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

import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingBiFunction;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckApiClient {
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import", BlackDuckStringResponse.class, false);
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries", BlackDuckResponse.class, false);
    public static final BlackDuckPath UPLOADS_PATH = new BlackDuckPath("/api/uploads", BlackDuckStringResponse.class, false);
    public static final BlackDuckPath SCAN_DATA_PATH = new BlackDuckPath("/api/scan/data/", BlackDuckResponse.class, false); // TODO: Determine why this endpoint requires a slash at the end.
    public static final BlackDuckPath SCAN_DEVELOPER_MODE_PATH = new BlackDuckPath("/api/developer-scans", BlackDuckResponse.class, false);
    public static final BlackDuckPath SCAN_INTELLIGENT_PERSISTENCE_MODE_PATH = new BlackDuckPath("/api/intelligent-persistence-scans", BlackDuckResponse.class, false);

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;

    public BlackDuckApiClient(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
        BlackDuckResponsesTransformer blackDuckResponsesTransformer, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.blackDuckResponseTransformer = blackDuckResponseTransformer;
        this.blackDuckResponsesTransformer = blackDuckResponsesTransformer;
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(UrlMultipleResponses<T> urlMultipleResponses, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getSomeMatchingResponses(urlMultipleResponses, blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(), predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder requestBuilder, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getBlackDuckViewResponses(urlMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, predicate, totalLimit));
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(UrlMultipleResponses<T> urlMultipleResponses) throws IntegrationException {
        return getAllResponses(urlMultipleResponses, blackDuckRequestBuilderFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        return getBlackDuckViewResponses(urlMultipleResponses, requestBuilder, blackDuckResponsesTransformer::getAllResponses);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(UrlMultipleResponses<T> urlMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(urlMultipleResponses, blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder requestBuilder, int totalLimit) throws IntegrationException {
        return getBlackDuckViewResponses(urlMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit));
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        HttpUrl url = urlMultipleResponses.getUrl();
        Class<T> responseClass = urlMultipleResponses.getResponseClass();
        return getPageResponse(url, responseClass, blackDuckPageDefinition);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder) throws IntegrationException {
        HttpUrl url = urlMultipleResponses.getUrl();
        Class<T> responseClass = urlMultipleResponses.getResponseClass();
        return getPageResponse(url, responseClass, blackDuckRequestBuilder);
    }

    public <T extends BlackDuckResponse> T getResponse(UrlSingleResponse<T> urlSingleResponse) throws IntegrationException {
        return getResponse(urlSingleResponse, blackDuckRequestBuilderFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> T getResponse(UrlSingleResponse<T> urlSingleResponse, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        HttpUrl url = urlSingleResponse.getUrl();
        return blackDuckResponseTransformer.getResponse(requestBuilder.build(url), urlSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> Optional<T> getResponseSafely(UrlSingleResponse<T> urlSingleResponse) throws IntegrationException {
        try {
            HttpUrl url = urlSingleResponse.getUrl();
            Request request = blackDuckRequestBuilderFactory.createCommonGetRequest(url);
            return Optional.of(blackDuckResponseTransformer.getResponse(request, urlSingleResponse.getResponseClass()));
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

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(HttpUrl url, Class<T> responseClass, BlackDuckRequestBuilder blackDuckRequestBuilder) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(url);
        return getPageResponse(requestBuilder, responseClass);
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
        requestBuilder.setBlackDuckPageDefinition(blackDuckPageDefinition);
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        return blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, responseClass);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass) throws IntegrationException {
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        return blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, responseClass);
    }

    public <T extends BlackDuckResponse> T getResponse(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        Request request = blackDuckRequestBuilderFactory.createCommonGetRequest(url);
        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public HttpUrl post(HttpUrl url, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        Request request = blackDuckRequestBuilderFactory.createCommonPostRequestBuilder(blackDuckComponent).build(url);
        return executePostRequestAndRetrieveURL(request);
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

    private <T extends BlackDuckResponse> List<T> getBlackDuckViewResponses(UrlMultipleResponses<T> urlMultipleResponses, BlackDuckRequestBuilder requestBuilder,
        ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        HttpUrl url = urlMultipleResponses.getUrl();
        Class<T> responseClass = urlMultipleResponses.getResponseClass();
        return getSpecialResponses(url, responseClass, requestBuilder, responsesTransformer);
    }

    private <T extends BlackDuckResponse> List<T> getSpecialResponses(HttpUrl httpUrl, Class<T> responseClass, BlackDuckRequestBuilder requestBuilder,
        ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        requestBuilder.url(httpUrl);
        return responsesTransformer.apply(new PagedRequest(requestBuilder), responseClass).getItems();
    }

}
