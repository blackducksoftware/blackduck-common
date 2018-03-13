package com.blackducksoftware.integration.hub.service.model;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubPath;

public class UriCombiner {
    public String pieceTogetherUri(final URL baseUrl, final HubPath hubPath) throws IntegrationException {
        return pieceTogetherUri(baseUrl, hubPath.getPath());
    }

    public String pieceTogetherUri(final URL baseUrl, final String path) throws IntegrationException {
        String uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(baseUrl.toURI());
            uriBuilder.setPath(path);
            uri = uriBuilder.build().toString();
        } catch (final URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
        return uri;
    }

}
