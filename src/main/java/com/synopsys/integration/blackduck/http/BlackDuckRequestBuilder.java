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

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.FileBodyContent;
import com.synopsys.integration.rest.body.MapBodyContent;
import com.synopsys.integration.rest.body.MultipartBodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckRequestBuilder {
    public static final String LIMIT_PARAMETER = "limit";
    public static final String OFFSET_PARAMETER = "offset";
    public static final String Q_PARAMETER = "q";
    public static final String FILTER_PARAMETER = "filter";

    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;

    private final Request.Builder requestBuilder;
    private final Gson gson;

    public BlackDuckRequestBuilder(BlackDuckRequestBuilder blackDuckRequestBuilder) {
        this(blackDuckRequestBuilder.gson, blackDuckRequestBuilder.requestBuilder);
    }

    public BlackDuckRequestBuilder(Gson gson, Request.Builder requestBuilder) {
        this.gson = gson;
        this.requestBuilder = new Request.Builder(requestBuilder);
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

    public BlackDuckRequestBuilder setLimitAndOffset(int limit, int offset) {
        return setBlackDuckPageDefinition(new BlackDuckPageDefinition(limit, offset));
    }

    public BlackDuckRequestBuilder setBlackDuckPageDefinition(BlackDuckPageDefinition blackDuckPageDefinition) {
        setLimit(blackDuckPageDefinition.getLimit());
        return setOffset(blackDuckPageDefinition.getOffset());
    }

    public BlackDuckRequestBuilder setLimit(int limit) {
        requestBuilder.setQueryParameter(LIMIT_PARAMETER, String.valueOf(limit));
        return this;
    }

    public BlackDuckRequestBuilder setOffset(int offset) {
        requestBuilder.setQueryParameter(OFFSET_PARAMETER, String.valueOf(offset));
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

    public BlackDuckRequestBuilder method(HttpMethod method) {
        requestBuilder.method(method);
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

    public BlackDuckRequestBuilder commonGet() {
        setBlackDuckPageDefinition(new BlackDuckPageDefinition(DEFAULT_LIMIT, DEFAULT_OFFSET));
        return get();
    }

    public BlackDuckRequestBuilder get() {
        return method(HttpMethod.GET);
    }

    public BlackDuckRequestBuilder postFile(File bodyContentFile) {
        return postBodyContent(new FileBodyContent(bodyContentFile));
    }

    public BlackDuckRequestBuilder postMap(Map<String, String> bodyContentMap) {
        return postBodyContent(new MapBodyContent(bodyContentMap));
    }

    public BlackDuckRequestBuilder postMultipart(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        return postBodyContent(new MultipartBodyContent(bodyContentFileMap, bodyContentStringMap));
    }

    public BlackDuckRequestBuilder postString(String bodyContent) {
        return postBodyContent(new StringBodyContent(bodyContent));
    }

    public BlackDuckRequestBuilder postObject(Object bodyContent) {
        return postBodyContent(new StringBodyContent(gson.toJson(bodyContent)));
    }

    public BlackDuckRequestBuilder postBodyContent(BodyContent bodyContent) {
        post();
        return bodyContent(bodyContent);
    }

    public BlackDuckRequestBuilder post() {
        return method(HttpMethod.POST);
    }

    public BlackDuckRequestBuilder putString(String bodyContent) {
        return putBodyContent(new StringBodyContent(bodyContent));
    }

    public BlackDuckRequestBuilder putObject(Object bodyContent) {
        return putBodyContent(new StringBodyContent(gson.toJson(bodyContent)));
    }

    public BlackDuckRequestBuilder putBodyContent(BodyContent bodyContent) {
        put();
        return bodyContent(bodyContent);
    }

    public BlackDuckRequestBuilder put() {
        return method(HttpMethod.PUT);
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
