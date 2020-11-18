package com.synopsys.integration.blackduck.http.client.cache;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;

public class CachingHttpClient implements BlackDuckHttpClient {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final Map<Request, Response> cache;

    public CachingHttpClient(BlackDuckHttpClient blackDuckHttpClient) {
        this(blackDuckHttpClient, new ConcurrentHashMap<>());
    }

    public CachingHttpClient(BlackDuckHttpClient blackDuckHttpClient, Map<Request, Response> cache) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.cache = cache;
    }

    public void emptyCache() {
        cache.clear();
    }

    @Override
    public Response execute(Request request) throws IntegrationException {
        if (HttpMethod.GET == request.getMethod() && cache.containsKey(request)) {
            return cache.get(request);
        }
        Response response = blackDuckHttpClient.execute(request);
        cache.put(request, response);
        return response;
    }

    @Override
    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        return blackDuckHttpClient.executeGetRequestIfModifiedSince(getRequest, timeToCheck);
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        return blackDuckHttpClient.attemptAuthentication();
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return blackDuckHttpClient.isAlreadyAuthenticated(request);
    }

    @Override
    public Optional<ErrorResponse> extractErrorResponse(String responseContent) {
        return blackDuckHttpClient.extractErrorResponse(responseContent);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        blackDuckHttpClient.handleErrorResponse(request, response);
    }

    @Override
    public void throwExceptionForError(Response response) throws IntegrationException {
        blackDuckHttpClient.throwExceptionForError(response);
    }

    @Override
    public HttpUrl getBaseUrl() {
        return blackDuckHttpClient.getBaseUrl();
    }

    @Override
    public String getUserAgentString() {
        return blackDuckHttpClient.getUserAgentString();
    }

    @Override
    public HttpClientBuilder getHttpClientBuilder() {
        return blackDuckHttpClient.getHttpClientBuilder();
    }

}
