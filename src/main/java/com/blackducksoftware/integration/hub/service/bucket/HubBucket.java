package com.blackducksoftware.integration.hub.service.bucket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class HubBucket {
    private final Map<String, HubBucketItem<? extends HubResponse>> bucket = new HashMap<>();

    public boolean contains(final String uri) {
        return bucket.containsKey(uri);
    }

    public Set<String> getAvailableUris() {
        return bucket.keySet();
    }

    public HubBucketItem<? extends HubResponse> get(final String uri) {
        return bucket.get(uri);
    }

    public Optional<? extends HubResponse> getResponse(final String uri) {
        return bucket.get(uri).getHubResponse();
    }

    public Optional<Exception> getError(final String uri) {
        return bucket.get(uri).getE();
    }

    public void addValid(final String uri, final HubResponse hubResponse) {
        bucket.put(uri, new HubBucketItem<>(uri, hubResponse));
    }

    public void addError(final String uri, final Exception e) {
        bucket.put(uri, new HubBucketItem<>(uri, e));
    }

    public HubBucketItem<? extends HubResponse> remove(final String uri) {
        return bucket.remove(uri);
    }

}
