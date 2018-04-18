package com.blackducksoftware.integration.hub.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.log.IntLogger;

public class HubCacheService extends DataService {
    public HubCacheService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
    }

    public Map<String, HubResponse> requestAllResponses(final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses) throws IntegrationException {
        final Map<String, HubResponse> allResponses = new HashMap<>();
        for (final UriSingleResponse<? extends HubResponse> uriSingleResponse : uriSingleResponses) {
            final HubResponse hubResponse = hubService.getResponse(uriSingleResponse);
            allResponses.put(uriSingleResponse.uri, hubResponse);
        }

        return allResponses;
    }

}
