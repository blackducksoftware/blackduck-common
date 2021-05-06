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
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class BlackDuckResponsesTransformerTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer = blackDuckServicesFactory.getBlackDuckResponsesTransformer();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory = blackDuckServicesFactory.getBlackDuckRequestBuilderFactory();

    BlackDuckResponsesTransformerTestIT() throws IntegrationException {
    }

    @Test
    void getAllResponses() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();

        BlackDuckRequest<ProjectView> blackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink().getResponseClass());
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());
    }

    @Test
    void getResponsesWithAll() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();

        BlackDuckRequest<ProjectView> defaultBlackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink().getResponseClass());
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(defaultBlackDuckRequest);
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());

        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2, 0));
        BlackDuckRequest<ProjectView> limitedBlackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink().getResponseClass());
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getOnePageOfResponses(limitedBlackDuckRequest);
        Assertions.assertEquals(2, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 2 projects for this test to pass.");
    }

    @Test
    void testGetResponsesWithMaxLimit() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();

        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(5, 0));
        BlackDuckRequest<ProjectView> oversizedBlackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink().getResponseClass());
        BlackDuckPageResponse<ProjectView> underPageSizeResponse = blackDuckResponsesTransformer.getSomeResponses(oversizedBlackDuckRequest, 2);
        Assertions.assertEquals(2, underPageSizeResponse.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");

        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2, 0));
        BlackDuckRequest<ProjectView> limitedBlackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, apiDiscovery.metaProjectsLink().getResponseClass());
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getSomeResponses(limitedBlackDuckRequest, 5);
        Assertions.assertEquals(5, limitedResponses.getItems().size(), "Too many projects were returned. Note: Black Duck must have more than 5 projects for this test to pass.");
    }

    private BlackDuckRequestBuilder createBlackDuckRequestBuilder() throws IntegrationException {
        return blackDuckRequestBuilderFactory
                   .createCommonGet()
                   .url(apiDiscovery.metaProjectsLink().getUrl());
    }

}
