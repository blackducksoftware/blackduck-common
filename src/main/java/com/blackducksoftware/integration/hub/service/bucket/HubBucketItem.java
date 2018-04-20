package com.blackducksoftware.integration.hub.service.bucket;

import java.util.Optional;

import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class HubBucketItem<T extends HubResponse> {
    private final String uri;
    private final Optional<T> hubResponse;
    private final Optional<Exception> e;

    public HubBucketItem(final String uri, final T hubResponse) {
        this.uri = uri;
        this.hubResponse = Optional.of(hubResponse);
        this.e = Optional.empty();
    }

    public HubBucketItem(final String uri, final Exception e) {
        this.uri = uri;
        this.hubResponse = Optional.empty();
        this.e = Optional.of(e);
    }

    public boolean hasException() {
        return e.isPresent();
    }

    public boolean hasValidResponse() {
        return !e.isPresent();
    }

    public String getUri() {
        return uri;
    }

    public Optional<T> getHubResponse() {
        return hubResponse;
    }

    public Optional<Exception> getE() {
        return e;
    }

}
