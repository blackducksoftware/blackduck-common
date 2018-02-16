package com.blackducksoftware.integration.hub.service.model;

import static com.blackducksoftware.integration.hub.RestConstants.QUERY_LIMIT;
import static com.blackducksoftware.integration.hub.RestConstants.QUERY_OFFSET;

import com.blackducksoftware.integration.hub.request.Request;

public class PagedRequest {
    private final Request.Builder requestBuilder;
    private final int offset;
    private final int limit;

    public PagedRequest(final Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
        this.offset = 0;
        this.limit = 100;
    }

    public PagedRequest(final Request.Builder requestBuilder, final int offset, final int limit) {
        this.requestBuilder = requestBuilder;
        this.offset = offset;
        this.limit = limit;
    }

    public Request createRequest() {
        final Request request = requestBuilder.build();
        request.getQueryParameters().put(QUERY_LIMIT, String.valueOf(getLimit()));
        request.getQueryParameters().put(QUERY_OFFSET, String.valueOf(getOffset()));
        return request;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

}
