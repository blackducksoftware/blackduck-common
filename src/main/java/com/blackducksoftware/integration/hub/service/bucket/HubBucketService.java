package com.blackducksoftware.integration.hub.service.bucket;

import java.util.List;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.service.DataService;
import com.blackducksoftware.integration.hub.service.HubService;

public class HubBucketService extends DataService {
    public HubBucketService(final HubService hubService) {
        super(hubService);
    }

    public HubBucket startTheBucket(final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) {
        final HubBucket hubBucket = new HubBucket();
        addToTheBucket(hubBucket, uriSingleResponses);
        return hubBucket;
    }

    public void addToTheBucket(final HubBucket hubBucket, final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) {
        for (final UriSingleResponse<? extends HubResponse> uriSingleResponse : uriSingleResponses) {
            if (!hubBucket.contains(uriSingleResponse.uri)) {
                try {
                    final HubResponse hubResponse = hubService.getResponse(uriSingleResponse);
                    hubBucket.addValid(uriSingleResponse.uri, hubResponse);
                } catch (final Exception e) {
                    // it is up to the consumer of the bucket to log or handle any/all Exceptions
                    hubBucket.addError(uriSingleResponse.uri, e);
                }
            }
        }
    }

}
