package com.blackduck.integration.blackduck.http.transform;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TimingExtension.class)
public class BlackDuckResponsesTransformerTest {
    @Test
    public void testMatchingAcrossAllPages() throws IOException, IntegrationException {
        assertMatching(1);
    }

    @Test
    public void testMatchingAcrossAllPagesLargeLimit() throws IOException, IntegrationException {
        assertMatching(Integer.MAX_VALUE);
    }

    private void assertMatching(int limit) throws IOException, IntegrationException {
        MockedClient mockedClient = new MockedClient();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = mockedClient.blackDuckRequest;
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = mockedClient.blackDuckResponsesTransformer;

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        int listCount = allPagesResponse.getItems().size();

        // we go through every item to ensure we match across page boundaries
        for (int i = 0; i < listCount; i++) {
            String myTestUrl = allPagesResponse.getItems().get(i).getHref().string();
            String customErrorMessage = String.format("Failed for limit %d, test %d of %d, testing:", limit, i, listCount - 1);

            Predicate<ProjectView> predicate = httpUrl -> myTestUrl.equalsIgnoreCase(httpUrl.getHref().string());
            BlackDuckMultipleRequest<ProjectView> innerBlackDuckRequest = new MockedClient().blackDuckRequest;
            BlackDuckPageResponse<ProjectView> matchedResponse = blackDuckResponsesTransformer.getSomeMatchingResponses(innerBlackDuckRequest, predicate, limit);

            assertEquals(69, matchedResponse.getTotalCount(), String.format("%s total response retrieved", customErrorMessage));
            assertEquals(1, matchedResponse.getItems().size(), String.format("%s match count returned", customErrorMessage));
            assertEquals(myTestUrl, matchedResponse.getItems().get(0).getHref().string(), String.format("%s return url matches", customErrorMessage));
        }

        System.out.println(String.format("%d Matching tests successful for limit %d", listCount, limit));
    }

    @Test
    public void testNoMatchingAcrossAllPages() throws IOException, IntegrationException {
        MockedClient mockedClient = new MockedClient();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = mockedClient.blackDuckRequest;
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = mockedClient.blackDuckResponsesTransformer;
        String myTestUrl = "https://www.no-match.com";
        Predicate<ProjectView> predicate = httpUrl -> myTestUrl.equalsIgnoreCase(httpUrl.getHref().string());
        BlackDuckPageResponse<ProjectView> matchedResponse = blackDuckResponsesTransformer.getSomeMatchingResponses(blackDuckRequest, predicate, 1);

        assertEquals(69, matchedResponse.getTotalCount());
        assertEquals(0, matchedResponse.getItems().size(), "Should not have received any matched responses.");
    }

    @Test
    public void testGettingAllOnePageTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_all_on_one_page.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 100);

        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, BlackDuckServicesFactory.createDefaultObjectMapper(), blackDuckResponseResolver, new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .addQueryParameter(BlackDuckRequestBuilder.LIMIT_PARAMETER, "100")
                                                              .addQueryParameter(BlackDuckRequestBuilder.OFFSET_PARAMETER, "0");
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = BlackDuckRequest.createMultipleRequest(blackDuckRequestBuilder, new HttpUrl("https://blackduckserver.com/api/projects"), ProjectView.class);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingAllMultiplePagesTotal() throws IntegrationException, IOException {
        MockedClient mockedClient = new MockedClient();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = mockedClient.blackDuckRequest;
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = mockedClient.blackDuckResponsesTransformer;

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingOnePageOnePageTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_all_on_one_page.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 100);

        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, BlackDuckServicesFactory.createDefaultObjectMapper(), blackDuckResponseResolver, new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .addQueryParameter(BlackDuckRequestBuilder.LIMIT_PARAMETER, "100")
                                                              .addQueryParameter(BlackDuckRequestBuilder.OFFSET_PARAMETER, "0");
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = BlackDuckRequest.createMultipleRequest(blackDuckRequestBuilder, new HttpUrl("https://blackduckserver.com/api/projects"), ProjectView.class);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getOnePageOfResponses(blackDuckRequest);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingOnePageMultiplePagesTotal() throws IOException, IntegrationException {
        MockedClient mockedClient = new MockedClient();
        BlackDuckMultipleRequest<ProjectView> blackDuckRequest = mockedClient.blackDuckRequest;
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = mockedClient.blackDuckResponsesTransformer;

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getOnePageOfResponses(blackDuckRequest);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(20, allPagesResponse.getItems().size());
    }

    private void mockClientBehavior(BlackDuckHttpClient blackDuckHttpClient, Map<String, String> offsetsToResults, int limit) throws IOException, IntegrationException {
        Set<String> knownsOffsets = offsetsToResults.keySet();

        for (Map.Entry<String, String> entry : offsetsToResults.entrySet()) {
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getContentString()).thenReturn(getText(entry.getValue()));

            ArgumentMatcher<BlackDuckRequest> argRequest = createRequestMatcher(new HttpUrl("https://blackduckserver.com/api/projects"), Integer.parseInt(entry.getKey()), limit);
            Mockito.when(blackDuckHttpClient.execute(Mockito.argThat(argRequest))).thenReturn(response);
        }

        ArgumentMatcher<BlackDuckRequest> unknownOffsetRequest = request -> {
            String requestOffset = request.getRequest().getQueryParameters().get(BlackDuckRequestBuilder.OFFSET_PARAMETER).stream().findFirst().get();
            return !knownsOffsets.contains(requestOffset);
        };
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getContentString()).thenReturn(getText("projectViews_empty.json"));
        Mockito.when(blackDuckHttpClient.execute(Mockito.argThat(unknownOffsetRequest))).thenReturn(response);
    }

    private ArgumentMatcher<BlackDuckRequest> createRequestMatcher(HttpUrl url, int offset, int limit) {
        return new ArgumentMatcher<BlackDuckRequest>() {
            @Override
            public boolean matches(BlackDuckRequest request) {
                if (null != request && request.getUrl().equals(url)) {
                    String requestOffset = request.getRequest().getQueryParameters().get(BlackDuckRequestBuilder.OFFSET_PARAMETER).stream().findFirst().get();
                    return requestOffset.equals(Integer.toString(offset));
                }
                return false;
            }
        };
    }

    private String getText(String resourceName) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/BlackDuckResponsesTransformer/" + resourceName), StandardCharsets.UTF_8);
    }

    private class MockedClient {
        private final BlackDuckMultipleRequest<ProjectView> blackDuckRequest;
        private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;

        public MockedClient() throws IOException, IntegrationException {
            Map<String, String> offsetsToResults = new HashMap<>();
            offsetsToResults.put("0", "projectViews_page_1_of_4.json");
            offsetsToResults.put("20", "projectViews_page_2_of_4.json");
            offsetsToResults.put("40", "projectViews_page_3_of_4.json");
            offsetsToResults.put("60", "projectViews_page_4_of_4.json");

            BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
            mockClientBehavior(blackDuckHttpClient, offsetsToResults, 20);

            Gson gson = BlackDuckServicesFactory.createDefaultGson();
            BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
            BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, BlackDuckServicesFactory.createDefaultObjectMapper(), blackDuckResponseResolver, new PrintStreamIntLogger(System.out, LogLevel.INFO));

            BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                                  .addQueryParameter(BlackDuckRequestBuilder.LIMIT_PARAMETER, "20")
                                                                  .addQueryParameter(BlackDuckRequestBuilder.OFFSET_PARAMETER, "0");
            blackDuckRequest = BlackDuckRequest.createMultipleRequest(blackDuckRequestBuilder, new HttpUrl("https://blackduckserver.com/api/projects"), ProjectView.class);
            blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        }
    }

}
