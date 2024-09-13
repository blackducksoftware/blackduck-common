package com.blackduck.integration.blackduck.http.transform;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.comprehensive.recipe.BasicRecipe;
import com.blackduck.integration.blackduck.http.BlackDuckPageDefinition;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TimingExtension.class)
class BlackDuckResponsesTransformerTestIT extends BasicRecipe {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer = blackDuckServicesFactory.getBlackDuckResponsesTransformer();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();

    BlackDuckResponsesTransformerTestIT() throws IntegrationException {
    }

    @AfterAll
    public void cleanup() throws IntegrationException {
        deleteCreatedProjects();
    }

    @Test
    void getAllResponses() throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = blackDuckRequestBuilder.buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);

        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());
    }

    @Test
    void getResponsesWithAll() throws IntegrationException {
        createThrowAwayProjects(3);

        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = blackDuckRequestBuilder.buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> responses = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        Assertions.assertEquals(responses.getTotalCount(), responses.getItems().size());

        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2, 0));
        BlackDuckMultipleRequest<ProjectView> limitedBlackDuckRequest = blackDuckRequestBuilder.buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getOnePageOfResponses(limitedBlackDuckRequest);

        Assertions.assertEquals(2, limitedResponses.getItems().size(), "Incorrect number of projects returned. Note: Black Duck must have 2 or more projects for this test to pass.");
    }

    @Test
    void testGetResponsesWithMaxLimit() throws IntegrationException {
        createThrowAwayProjects(6);

        BlackDuckRequestBuilder blackDuckRequestBuilder = createBlackDuckRequestBuilder();
        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(5, 0));
        BlackDuckMultipleRequest<ProjectView> oversizedBlackDuckRequest = blackDuckRequestBuilder.buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> underPageSizeResponse = blackDuckResponsesTransformer.getSomeResponses(oversizedBlackDuckRequest, 2);

        Assertions.assertEquals(2, underPageSizeResponse.getItems().size(), "Incorrect number of projects returned. Note: Black Duck must have 2 or more projects for this test to pass.");

        blackDuckRequestBuilder.setBlackDuckPageDefinition(new BlackDuckPageDefinition(2, 0));
        BlackDuckMultipleRequest<ProjectView> limitedBlackDuckRequest = blackDuckRequestBuilder.buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> limitedResponses = blackDuckResponsesTransformer.getSomeResponses(limitedBlackDuckRequest, 5);

        Assertions.assertEquals(5, limitedResponses.getItems().size(), "Incorrect number of projects returned. Note: Black Duck must have 5 or more projects for this test to pass.");
    }

    private BlackDuckRequestBuilder createBlackDuckRequestBuilder() throws IntegrationException {
        return new BlackDuckRequestBuilder()
            .commonGet()
            .url(apiDiscovery.metaProjectsLink().getUrl());
    }

}
