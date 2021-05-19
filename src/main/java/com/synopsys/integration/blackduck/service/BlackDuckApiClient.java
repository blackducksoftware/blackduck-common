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
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
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

    public <T extends BlackDuckResponse> List<T> getAllResponses(UrlMultipleResponses<T> urlMultipleResponses) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(createCommonGetRequestBuilder(), urlMultipleResponses);
        return getAllResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return blackDuckResponsesTransformer.getAllResponses(requestMultiple).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(UrlMultipleResponses<T> urlMultipleResponses, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createCommonGetRequestBuilder();
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, urlMultipleResponses);
        return getSomeMatchingResponses(requestMultiple, predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeMatchingResponses(requestMultiple, predicate, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(UrlMultipleResponses<T> urlMultipleResponses, int totalLimit) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(createCommonGetRequestBuilder(), urlMultipleResponses);
        return getSomeResponses(requestMultiple, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeResponses(requestMultiple, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return blackDuckResponsesTransformer.getOnePageOfResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> T getResponse(UrlSingleResponse<T> urlSingleResponse) throws IntegrationException {
        BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle = new BlackDuckRequest<>(createCommonGetRequestBuilder(), urlSingleResponse);
        return getResponse(requestSingle);
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle) throws IntegrationException {
        return blackDuckResponseTransformer.getResponse(requestSingle);
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckRequestBuilder
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, url, responseClass, UrlMultipleResponses::new);
        return blackDuckResponsesTransformer.getAllResponses(requestMultiple).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass, int totalLimit) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, url, responseClass, UrlMultipleResponses::new);
        return blackDuckResponsesTransformer.getSomeResponses(requestMultiple, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass, BlackDuckPageDefinition blackDuckPageDefinition)
        throws IntegrationException {
        blackDuckRequestBuilder.setBlackDuckPageDefinition(blackDuckPageDefinition);
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, url, responseClass, UrlMultipleResponses::new);
        return blackDuckResponsesTransformer.getOnePageOfResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, url, responseClass, UrlMultipleResponses::new);
        return blackDuckResponsesTransformer.getOnePageOfResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> T getResponse(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createCommonGetRequestBuilder();
        BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle = new BlackDuckRequest<>(blackDuckRequestBuilder, url, responseClass, UrlSingleResponse::new);
        return blackDuckResponseTransformer.getResponse(requestSingle);
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public HttpUrl post(HttpUrl url, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        Request request = blackDuckRequestBuilderFactory
                              .createBlackDuckRequestBuilder()
                              .postObject(blackDuckComponent)
                              .url(url)
                              .build();
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
        Request request = blackDuckRequestBuilderFactory
                              .createBlackDuckRequestBuilder()
                              .putString(json)
                              .url(url)
                              .build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(HttpUrl url) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory
                                                              .createBlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .url(url);
        Request request = blackDuckRequestBuilder.build();
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

    private BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url) {
        return createCommonGetRequestBuilder()
                   .url(url);
    }

    private BlackDuckRequestBuilder createCommonGetRequestBuilder() {
        return blackDuckRequestBuilderFactory.createCommonGet();
    }

}
