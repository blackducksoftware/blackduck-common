package com.blackducksoftware.integration.hub.service.bucket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class HubBucket {
    private final Map<String, HubBucketItem<? extends HubResponse>> bucket = new HashMap<>();

    public boolean contains(final String uri) {
        return bucket.containsKey(uri);
    }

    public Optional<? extends HubResponse> getResponse(final String uri) {
        return bucket.get(uri).getHubResponse();
    }

    public void addValidResponse(final String uri, final HubResponse hubResponse) {
        bucket.put(uri, new HubBucketItem<>(uri, hubResponse));
    }

    public void addErrorResponse(final String uri, final Exception e) {
        bucket.put(uri, new HubBucketItem<>(uri, e));
    }

    public HubBucketItem<? extends HubResponse> remove(final String uri) {
        return bucket.remove(uri);
    }

}
