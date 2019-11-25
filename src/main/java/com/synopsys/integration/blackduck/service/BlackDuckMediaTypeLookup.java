package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.blackduck.service.model.RequestFactory;

public class BlackDuckMediaTypeLookup {
    private final MediaTypeDiscovery mediaTypeDiscovery;

    public BlackDuckMediaTypeLookup(MediaTypeDiscovery mediaTypeDiscovery) {
        this.mediaTypeDiscovery = mediaTypeDiscovery;
    }

    public <T extends BlackDuckResponse> String findMediaType(Class<T> clazz) {
        return mediaTypeDiscovery.determineMediaType(clazz).orElse(RequestFactory.DEFAULT_MEDIA_TYPE);
    }
}
