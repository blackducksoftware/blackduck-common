package com.blackduck.integration.blackduck.service;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.http.client.TestingPropertyKey;
import com.blackduck.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.blackduck.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.blackduck.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.request.AcceptHeaderEditor;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.blackduck.service.request.BlackDuckSingleRequest;
import com.blackduck.integration.blackduck.service.request.PagingDefaultsEditor;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckApiClientTestIT {
    private final IntHttpClientTestHelper testHelper = new IntHttpClientTestHelper();

    @Test
    public void testMediaType() throws IntegrationException {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        BlackDuckMediaTypeDiscoveryVerifier blackDuckMediaTypeDiscoveryVerifier = new BlackDuckMediaTypeDiscoveryVerifier();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUrl(testHelper.getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL));
        blackDuckServerConfigBuilder.setUsername(testHelper.getProperty(TestingPropertyKey.TEST_USERNAME));
        blackDuckServerConfigBuilder.setPassword(testHelper.getProperty(TestingPropertyKey.TEST_PASSWORD));
        blackDuckServerConfigBuilder.setTrustCert(Boolean.parseBoolean(testHelper.getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfigBuilder.build().createBlackDuckHttpClient(logger);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckApiClient blackDuckApiClient = new BlackDuckApiClient(blackDuckHttpClient, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer);
        ApiDiscovery apiDiscovery = new ApiDiscovery(blackDuckServerConfigBuilder.build().getBlackDuckUrl());

        assertNull(blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertNull(blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder().commonGet();
        BlackDuckMultipleRequest<ProjectView> requestMultiple = new BlackDuckMultipleRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink(), new PagingDefaultsEditor(),
            new AcceptHeaderEditor(blackDuckMediaTypeDiscoveryVerifier));
        List<ProjectView> projects = blackDuckApiClient.getSomeResponses(requestMultiple, 5);
        assertTrue(projects.size() > 0);
        assertEquals(null, blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        blackDuckRequestBuilder = new BlackDuckRequestBuilder().commonGet();
        ProjectView firstProject = projects.get(0);
        UrlSingleResponse<ProjectView> projectViewUrlSingleResponse = new UrlSingleResponse<>(firstProject.getHref(), ProjectView.class);
        BlackDuckSingleRequest<ProjectView> requestSingle = new BlackDuckSingleRequest<>(blackDuckRequestBuilder, projectViewUrlSingleResponse, new PagingDefaultsEditor(), new AcceptHeaderEditor(blackDuckMediaTypeDiscoveryVerifier));
        ProjectView retrievedById = blackDuckApiClient.getResponse(requestSingle);
        assertEquals(null, blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/vnd.blackducksoftware.project-detail-6+json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);
    }

    private class BlackDuckMediaTypeDiscoveryVerifier extends BlackDuckMediaTypeDiscovery {
        public String originalMediaType;
        public String discoveredMediaType;

        @Override
        public String determineMediaType(HttpUrl url, String currentMediaType) {
            originalMediaType = currentMediaType;
            discoveredMediaType = super.determineMediaType(url, currentMediaType);
            return discoveredMediaType;
        }
    }

}
