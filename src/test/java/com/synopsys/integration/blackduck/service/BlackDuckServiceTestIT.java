package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckServiceTestIT {
    private IntHttpClientTestHelper testHelper = new IntHttpClientTestHelper();

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
        Gson gson = new Gson();
        ObjectMapper objectMapper = new ObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        RequestFactory requestFactory = new RequestFactory();

        BlackDuckService blackDuckService = new BlackDuckService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, requestFactory);
        assertNull(blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertNull(blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        List<ProjectView> projects = blackDuckService.getSomeResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, 5);
        assertTrue(projects.size() > 0);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.originalMediaType);
        assertEquals("application/json", blackDuckMediaTypeDiscoveryVerifier.discoveredMediaType);

        ProjectView firstProject = projects.get(0);
        ProjectView retrievedById = blackDuckService.getResponse(firstProject.getHref(), ProjectView.class);
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
