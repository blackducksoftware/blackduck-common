package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.service.request.AcceptHeaderEditor;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.blackduck.service.request.BlackDuckSingleRequest;
import com.synopsys.integration.blackduck.service.request.PagingDefaultsEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckApiClientTestIT {
    private final IntHttpClientTestHelper testHelper = new IntHttpClientTestHelper();

    @Test
    public void testMediaType() throws IntegrationException {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        BlackDuckMediaTypeDiscoveryVerifier blackDuckMediaTypeDiscoveryVerifier = new BlackDuckMediaTypeDiscoveryVerifier();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
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
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        blackDuckRequestBuilder = new BlackDuckRequestBuilder().commonGet();
        ProjectView firstProject = projects.get(0);
        UrlSingleResponse<ProjectView> projectViewUrlSingleResponse = new UrlSingleResponse<>(firstProject.getHref(), ProjectView.class);
        BlackDuckSingleRequest<ProjectView> requestSingle = new BlackDuckSingleRequest<>(blackDuckRequestBuilder, projectViewUrlSingleResponse, new PagingDefaultsEditor(), new AcceptHeaderEditor(blackDuckMediaTypeDiscoveryVerifier));
        ProjectView retrievedById = blackDuckApiClient.getResponse(requestSingle);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/vnd.blackducksoftware.project-detail-4+json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);
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
