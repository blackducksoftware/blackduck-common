package com.synopsys.integration.blackduck.http.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class BlackDuckResponsesTransformerTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory = new BlackDuckRequestBuilderFactory(new Gson());

    BlackDuckResponsesTransformerTestIT() throws IntegrationException {
    }

    @Test
    void getAllResponses() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultPagedRequest, apiDiscovery.metaProjectsLink().getResponseClass());
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());
    }

    @Test
    void getResponsesWithAll() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        PagedRequest defaultPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultPagedRequest, apiDiscovery.metaProjectsLink().getResponseClass());
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());

        requestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2,0));
        PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getOnePageOfResponses(limitedPagedRequest, apiDiscovery.metaProjectsLink().getResponseClass());
        Assertions.assertEquals(2, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 2 projects for this test to pass.");
    }

    @Test
    void testGetResponsesWithMaxLimit() throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = createBlackDuckResponsesTransformer();
        BlackDuckRequestBuilder requestBuilder = createRequestBuilder();

        requestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(5,0));
        PagedRequest oversizePagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> underPageSizeResponse = blackDuckResponsesTransformer.getSomeResponses(oversizePagedRequest, apiDiscovery.metaProjectsLink().getResponseClass(), 2);
        Assertions.assertEquals(2, underPageSizeResponse.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");

        requestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2,0));
        PagedRequest limitedPagedRequest = new PagedRequest(requestBuilder);
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getSomeResponses(limitedPagedRequest, apiDiscovery.metaProjectsLink().getResponseClass(), 5);
        Assertions.assertEquals(5, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");
    }

    private BlackDuckRequestBuilder createRequestBuilder() throws IntegrationException {
        return blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(apiDiscovery.metaProjectsLink().getUrl());
    }

    private BlackDuckResponsesTransformer createBlackDuckResponsesTransformer() {
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(blackDuckServicesFactory.getGson());
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(blackDuckServicesFactory.getGson(), blackDuckServicesFactory.getObjectMapper(), blackDuckResponseResolver, blackDuckServicesFactory.getLogger());
        return new BlackDuckResponsesTransformer(blackDuckServicesFactory.getBlackDuckHttpClient(), blackDuckJsonTransformer);
    }

}
