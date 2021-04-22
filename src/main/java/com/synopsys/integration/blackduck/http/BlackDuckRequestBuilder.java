/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.MultipartBodyContent;
import com.synopsys.integration.rest.request.Request;

public final class BlackDuckRequestBuilder {
    public static final String LIMIT_PARAMETER = "limit";
    public static final String OFFSET_PARAMETER = "offset";
    public static final String Q_PARAMETER = "q";
    public static final String FILTER_PARAMETER = "filter";

    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;
    public static final BlackDuckPageDefinition DEFAULT_PAGE = new BlackDuckPageDefinition(DEFAULT_LIMIT, DEFAULT_OFFSET);

    private final Request.Builder requestBuilder;

    public BlackDuckRequestBuilder() {
        this(new Request.Builder());
    }

    public BlackDuckRequestBuilder(HttpUrl url) {
        this(new Request.Builder(url));
    }

    public BlackDuckRequestBuilder(Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public Request build() {
        return requestBuilder.build();
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public BlackDuckRequestBuilder url(HttpUrl url) {
        requestBuilder.url(url);
        return this;
    }

    public BlackDuckRequestBuilder setBlackDuckPageDefinition(BlackDuckPageDefinition blackDuckPageDefinition) {
        requestBuilder.setQueryParameter(LIMIT_PARAMETER, String.valueOf(blackDuckPageDefinition.getLimit()));
        requestBuilder.setQueryParameter(OFFSET_PARAMETER, String.valueOf(blackDuckPageDefinition.getOffset()));
        return this;
    }

    public BlackDuckRequestBuilder setLimitAndOffset(int limit, int offset) {
        setBlackDuckPageDefinition(new BlackDuckPageDefinition(limit, offset));
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckQuery(Optional<BlackDuckQuery> blackDuckQuery) {
        if (blackDuckQuery.isPresent()) {
            requestBuilder.addQueryParameter(Q_PARAMETER, blackDuckQuery.get().getParameter());
        }
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckFilter(BlackDuckRequestFilter blackDuckRequestFilter) {
        if (blackDuckRequestFilter != null) {
            blackDuckRequestFilter.getFilterParameters().forEach(parameter -> {
                requestBuilder.addQueryParameter(FILTER_PARAMETER, parameter);
            });
        }
        return this;
    }

    public BlackDuckRequestBuilder get() {
        method(HttpMethod.GET);
        return this;
    }

    public BlackDuckRequestBuilder post() {
        method(HttpMethod.POST);
        return this;
    }

    public BlackDuckRequestBuilder put() {
        method(HttpMethod.PUT);
        return this;
    }

    public BlackDuckRequestBuilder delete() {
        method(HttpMethod.DELETE);
        return this;
    }

    public BlackDuckRequestBuilder method(HttpMethod method) {
        requestBuilder.method(method);
        return this;
    }

    public BlackDuckRequestBuilder commonGet() {
        get();
        setBlackDuckPageDefinition(DEFAULT_PAGE);
        return this;
    }

    public BlackDuckRequestBuilder acceptMimeType(String acceptHeader) {
        requestBuilder.acceptMimeType(acceptHeader);
        return this;
    }

    public BlackDuckRequestBuilder bodyEncoding(Charset bodyEncoding) {
        requestBuilder.bodyEncoding(bodyEncoding);
        return this;
    }

    public BlackDuckRequestBuilder queryParameters(Map<String, Set<String>> queryParameters) {
        requestBuilder.queryParameters(queryParameters);
        return this;
    }

    public BlackDuckRequestBuilder addQueryParameter(String key, String value) {
        requestBuilder.addQueryParameter(key, value);
        return this;
    }

    public BlackDuckRequestBuilder headers(Map<String, String> headers) {
        requestBuilder.headers(headers);
        return this;
    }

    public BlackDuckRequestBuilder addHeader(String key, String value) {
        requestBuilder.addHeader(key, value);
        return this;
    }

    public BlackDuckRequestBuilder bodyContent(BodyContent bodyContent) {
        requestBuilder.bodyContent(bodyContent);
        return this;
    }

    public BlackDuckRequestBuilder postMultipartBodyContent(Map<String, File> binaryParts, Map<String, String> textParts) {
        post();
        multipartBodyContent(binaryParts, textParts);
        return this;
    }

    public BlackDuckRequestBuilder multipartBodyContent(Map<String, File> binaryParts, Map<String, String> textParts) {
        requestBuilder.bodyContent(new MultipartBodyContent(binaryParts, textParts));
        return this;
    }

    public HttpUrl getUrl() {
        return requestBuilder.getUrl();
    }

    public HttpMethod getMethod() {
        return requestBuilder.getMethod();
    }

    public String getAcceptMimeType() {
        return requestBuilder.getAcceptMimeType();
    }

    public Charset getBodyEncoding() {
        return requestBuilder.getBodyEncoding();
    }

    public Map<String, Set<String>> getQueryParameters() {
        return requestBuilder.getQueryParameters();
    }

    public Map<String, String> getHeaders() {
        return requestBuilder.getHeaders();
    }

    public BodyContent getBodyContent() {
        return requestBuilder.getBodyContent();
    }

}
