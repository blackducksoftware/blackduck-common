/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.http.HttpHeaders;

import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.blackduck.version.BlackDuckVersion;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContentConverter;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckApiClient {
    //TODO ejk - create tests to exercise this endpoint - find out what version the slash is not required in
    public static final BlackDuckPath<BlackDuckResponse> SCAN_DATA_PATH = new BlackDuckPath<>("/api/scan/data/", BlackDuckResponse.class, false);

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private BlackDuckVersion blackDuckVersion;
    private BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery;

    public BlackDuckApiClient(BlackDuckHttpClient blackDuckHttpClient, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
        BlackDuckResponsesTransformer blackDuckResponsesTransformer) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.blackDuckResponseTransformer = blackDuckResponseTransformer;
        this.blackDuckResponsesTransformer = blackDuckResponsesTransformer;
        this.blackDuckVersion = null;
        this.blackDuckMediaTypeDiscovery = new BlackDuckMediaTypeDiscovery();
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(UrlMultipleResponses<T> urlMultipleResponses) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(new BlackDuckRequestBuilder().commonGet(), urlMultipleResponses);
        return getAllResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return blackDuckResponsesTransformer.getAllResponses(requestMultiple).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(UrlMultipleResponses<T> urlMultipleResponses, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder().commonGet();
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(blackDuckRequestBuilder, urlMultipleResponses);
        return getSomeMatchingResponses(requestMultiple, predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, Predicate<T> predicate, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeMatchingResponses(requestMultiple, predicate, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(UrlMultipleResponses<T> urlMultipleResponses, int totalLimit) throws IntegrationException {
        BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple = new BlackDuckRequest<>(new BlackDuckRequestBuilder().commonGet(), urlMultipleResponses);
        return getSomeResponses(requestMultiple, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeResponses(requestMultiple, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequest<T, UrlMultipleResponses<T>> requestMultiple) throws IntegrationException {
        return blackDuckResponsesTransformer.getOnePageOfResponses(requestMultiple);
    }

    public <T extends BlackDuckResponse> T getResponse(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        return getResponse(new UrlSingleResponse<>(url, responseClass));
    }

    public <T extends BlackDuckResponse> T getResponse(UrlSingleResponse<T> urlSingleResponse) throws IntegrationException {
        BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle = new BlackDuckRequest<>(new BlackDuckRequestBuilder().commonGet(), urlSingleResponse);
        return getResponse(requestSingle);
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckRequest<T, UrlSingleResponse<T>> requestSingle) throws IntegrationException {
        return blackDuckResponseTransformer.getResponse(requestSingle);
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public HttpUrl post(HttpUrl url, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .postObject(blackDuckComponent, BodyContentConverter.DEFAULT)
                                               .buildBlackDuckResponseRequest(url);
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
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .method(HttpMethod.DELETE)
                                               .buildBlackDuckResponseRequest(url);
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
        String mediaType = blackDuckMediaTypeDiscovery.determineMediaType(url);
        
        // add the 'missing' pieces back from view that could have been lost
        String json = blackDuckJsonTransformer.producePatchedJson(blackDuckView);
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .putString(json, BodyContentConverter.DEFAULT)
                                               .addHeader(HttpHeaders.CONTENT_TYPE, mediaType)
                                               .buildBlackDuckResponseRequest(url);
        try (Response response = execute(request)) {
            // TODO: Why do we not return the response here? JM - 07/2021
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(HttpUrl url) throws IntegrationException {
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder().buildBlackDuckResponseRequest(url);
        return execute(request);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response execute(BlackDuckResponseRequest request) throws IntegrationException {
        Response response = blackDuckHttpClient.execute(request);
        blackDuckHttpClient.throwExceptionForError(response);
        return response;
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public HttpUrl executePostRequestAndRetrieveURL(BlackDuckResponseRequest request) throws IntegrationException {
        try (Response response = execute(request)) {
            return new HttpUrl(response.getHeaderValue("location"));
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
    
    public Optional<BlackDuckVersion> getBlackDuckVersion() {
        return Optional.ofNullable(blackDuckVersion);
    }
    
    public void setBlackDuckVersion(BlackDuckVersion blackDuckVersion) {
        this.blackDuckVersion = blackDuckVersion;
    }

    private BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url) {
        return new BlackDuckRequestBuilder().url(url);
    }

}
