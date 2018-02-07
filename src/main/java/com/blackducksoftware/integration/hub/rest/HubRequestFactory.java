package com.blackducksoftware.integration.hub.rest;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Request;

public class HubRequestFactory {

    public String pieceTogetherURI(final URL baseUrl, final List<String> pathSegments) throws IntegrationException {
        return pieceTogetherURI(baseUrl, StringUtils.join(pathSegments, "/"));
    }

    public String pieceTogetherURI(final URL baseUrl, final String path) throws IntegrationException {
        try {
            final URIBuilder uriBuilder = new URIBuilder(baseUrl.toURI());
            uriBuilder.setPath(path);
            return uriBuilder.build().toString();
        } catch (final URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public Request createGetRequest(final String uri, final Map<String, String> queryParameters) {
        final Request request = new Request(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequest(final String uri, final Map<String, String> queryParameters) {
        final PagedRequest request = new PagedRequest(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequest(final String uri, final String q) {
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequest(final String uri, final String q, final int limit) {
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null, limit, 0);
        return request;
    }

    public Request createRequest(final String uri, final HttpMethod method) {
        final Request request = new Request(uri, null, null, method, null, null, null);
        return request;
    }

    // public Request createRequest(final String uri, final HttpMethod method, final String mimeType, final File bodyContent) {
    // final Request request = new Request(uri, null, null, method, mimeType, null, null);
    // request.setBodyContentFile(bodyContent);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method, final String mimeType, final String bodyContent) {
    // final Request request = new Request(uri, null, null, method, mimeType, null, null);
    // request.setBodyContent(bodyContent);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method, final String mimeType, final Map<String, String> bodyContent) {
    // final Request request = new Request(uri, null, null, method, mimeType, null, null);
    // request.setBodyContentMap(bodyContent);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method) {
    // final Request request = new Request(uri, null, null, method, null, null, null);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method, final File bodyContent) {
    // final Request request = new Request(uri, null, null, method, null, null, null);
    // request.setBodyContentFile(bodyContent);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method, final String bodyContent) {
    // final Request request = new Request(uri, null, null, method, null, null, null);
    // request.setBodyContent(bodyContent);
    // return request;
    // }
    //
    // public Request createRequest(final String uri, final HttpMethod method, final Map<String, String> bodyContent) {
    // final Request request = new Request(uri, null, null, method, null, null, null);
    // request.setBodyContentMap(bodyContent);
    // return request;
    // }

}
