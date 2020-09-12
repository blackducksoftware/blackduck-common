package com.synopsys.integration.blackduck.http.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class BlackDuckResponsesTransformerTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    private final RequestFactory requestFactory = new RequestFactory();

    BlackDuckResponsesTransformerTestIT() throws IntegrationException {
    }

    @Test
    void getAllResponses() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass());
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());
    }

    @Test
    void getResponsesWithAll() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass());
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());

        PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder, 0, 2);
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getOnePageOfResponses(limitedPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass());
        Assertions.assertEquals(2, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 2 projects for this test to pass.");
    }

    @Test
    void testGetResponsesWithMaxLimit() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        PagedRequest oversizePagedRequest = new PagedRequest(requestBuilder, 0, 5);
        BlackDuckPageResponse<ProjectView> underPageSizeResponse = blackDuckResponsesTransformer.getSomeResponses(oversizePagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), 2);
        Assertions.assertEquals(2, underPageSizeResponse.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");

        PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder, 0, 2);
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getSomeResponses(limitedPagedRequest, ApiDiscovery.PROJECTS_LINK_RESPONSE.getResponseClass(), 5);
        Assertions.assertEquals(5, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");
    }

    private BlackDuckRequestBuilder createRequestBuilder() throws IntegrationException {
        String blackDuckBaseUrl = intHttpClientTestHelper.getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL);
        HttpUrl url = new HttpUrl(blackDuckBaseUrl).appendRelativeUrl(ApiDiscovery.PROJECTS_LINK_RESPONSE.getBlackDuckPath().getPath());
        return requestFactory.createCommonGetRequestBuilder(url);
    }

    private BlackDuckResponsesTransformer createBlackDuckResponsesTransformer() {
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(blackDuckServicesFactory.getGson(), blackDuckServicesFactory.getObjectMapper(), blackDuckServicesFactory.getLogger());
        return new BlackDuckResponsesTransformer(blackDuckServicesFactory.getBlackDuckHttpClient(), blackDuckJsonTransformer);
    }

}
