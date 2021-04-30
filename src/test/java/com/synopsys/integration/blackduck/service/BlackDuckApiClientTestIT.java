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
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
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
        blackDuckServerConfigBuilder.setBlackDuckMediaTypeDiscovery(blackDuckMediaTypeDiscoveryVerifier);

        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfigBuilder.build().createBlackDuckHttpClient(logger);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory = new BlackDuckRequestBuilderFactory(gson);

        BlackDuckApiClient blackDuckApiClient = new BlackDuckApiClient(blackDuckHttpClient, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, blackDuckRequestBuilderFactory);
        ApiDiscovery apiDiscovery = new ApiDiscovery(blackDuckServerConfigBuilder.build().getBlackDuckUrl());

        assertNull(blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertNull(blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        List<ProjectView> projects = blackDuckApiClient.getSomeResponses(apiDiscovery.metaProjectsLink(), 5);
        assertTrue(projects.size() > 0);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        ProjectView firstProject = projects.get(0);
        ProjectView retrievedById = blackDuckApiClient.getResponse(firstProject.getHref(), ProjectView.class);
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
