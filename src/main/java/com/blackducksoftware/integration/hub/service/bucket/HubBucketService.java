package com.blackducksoftware.integration.hub.service.bucket;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.service.DataService;
import com.blackducksoftware.integration.hub.service.HubService;

public class HubBucketService extends DataService {
    public HubBucketService(final HubService hubService) {
        super(hubService);
    }

    public void addToTheBucket(final HubBucket hubBucket, final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        for (final UriSingleResponse<? extends HubResponse> uriSingleResponse : uriSingleResponses) {
            if (!hubBucket.contains(uriSingleResponse.uri)) {
                try {
                    final HubResponse hubResponse = hubService.getResponse(uriSingleResponse);
                    hubBucket.addValidResponse(uriSingleResponse.uri, hubResponse);
                } catch (final Exception e) {
                    // it is up to the consumer of the bucket to log or handle any/all Exceptions
                    hubBucket.addErrorResponse(uriSingleResponse.uri, e);
                }
            }
        }
    }

}
