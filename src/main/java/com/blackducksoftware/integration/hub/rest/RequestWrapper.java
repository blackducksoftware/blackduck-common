package com.blackducksoftware.integration.hub.rest;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class RequestWrapper {

    private final Map<String, String> queryParameters = new HashMap<>();
    private final Map<String, String> additionalHeaders = new HashMap<>();
    private String q;
    private String mimeType;
    private Charset bodyEncoding;
    private int limitPerRequest;

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public Charset getBodyEncoding() {
        return bodyEncoding;
    }

    public void setBodyEncoding(final Charset bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
    }

    public int getLimitPerRequest() {
        return limitPerRequest;
    }

    public void setLimitPerRequest(final int limitPerRequest) {
        this.limitPerRequest = limitPerRequest;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void addQueryParameter(final String key, final String value) {
        queryParameters.put(key, value);
    }

    public void addQueryParameters(final Map<String, String> parameters) {
        queryParameters.putAll(parameters);
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    public void addAdditionalHeader(final String key, final String value) {
        additionalHeaders.put(key, value);
    }

    public void addAdditionalHeaders(final Map<String, String> parameters) {
        additionalHeaders.putAll(parameters);
    }

}
