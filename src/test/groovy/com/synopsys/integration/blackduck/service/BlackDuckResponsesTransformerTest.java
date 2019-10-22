package com.synopsys.integration.blackduck.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class BlackDuckResponsesTransformerTest {

    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();

    BlackDuckResponsesTransformerTest() throws IntegrationException {}

    @Test
    void getAllResponses() throws IntegrationException {
        final BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        final Request.Builder requestBuilder = createRequestBuilder();

        final PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        final BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass());
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());
    }

    @Test
    void getResponsesWithAll() throws IntegrationException {
        final BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        final Request.Builder requestBuilder = createRequestBuilder();

        final PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        final BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getResponses(defaultPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), true);
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());

        final PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder, 0, 2);
        final BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getResponses(limitedPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), false);
        Assertions.assertEquals(2, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 2 projects for this test to pass.");
    }

    @Test
    void testGetResponsesWithMaxLimit() throws IntegrationException {
        final BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        final Request.Builder requestBuilder = createRequestBuilder();

        final PagedRequest oversizePagedRequest = new PagedRequest(requestBuilder, 0, 5);
        final BlackDuckPageResponse<ProjectView> underPageSizeResponse = blackDuckResponsesTransformer.getResponses(oversizePagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), 2);
        Assertions.assertEquals(2, underPageSizeResponse.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");

        final PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder, 0, 2);
        final BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getResponses(limitedPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), 5);
        Assertions.assertEquals(5, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");
    }

    private Request.Builder createRequestBuilder() throws BlackDuckIntegrationException {
        final String blackDuckBaseUrl = blackDuckServicesFactory.createBlackDuckService().getBlackDuckBaseUrl();
        final String uri = pieceTogetherUri(blackDuckBaseUrl, ApiDiscovery.PROJECTS_LINK_RESPONSE.getBlackDuckPath().getPath());
        return RequestFactory.createCommonGetRequestBuilder(uri);
    }

    private BlackDuckResponsesTransformer createBlackDuckResponsesTransformer() throws IntegrationException {
        final BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(blackDuckServicesFactory.getGson(), blackDuckServicesFactory.getObjectMapper(), blackDuckServicesFactory.getLogger());
        return new BlackDuckResponsesTransformer(blackDuckServicesFactory.getBlackDuckHttpClient(), blackDuckJsonTransformer);
    }

    private String pieceTogetherUri(String baseUrl, String spec) throws BlackDuckIntegrationException {
        URL url;
        try {
            URL baseURL = new URL(baseUrl);
            url = new URL(baseURL, spec);
        } catch (MalformedURLException e) {
            throw new BlackDuckIntegrationException(String.format("Could not construct the URL from %s and %s", baseUrl, spec), e);
        }
        return url.toString();
    }
}