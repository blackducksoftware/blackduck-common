package com.synopsys.integration.blackduck.http.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

@ExtendWith(TimingExtension.class)
public class BlackDuckResponsesTransformerTest {
    @Test
    public void danaHackTest() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_page_1_of_4.json");
        offsetsToResults.put("20", "projectViews_page_2_of_4.json");
        offsetsToResults.put("40", "projectViews_page_3_of_4.json");
        offsetsToResults.put("60", "projectViews_page_4_of_4.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 20);

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new MediaTypeDiscovery(), new Request.Builder());
        requestBuilder
            .url(new HttpUrl("https://blackduckserver.com/api/projects"))
            .addQueryParameter(RequestFactory.LIMIT_PARAMETER, "20")
            .addQueryParameter(RequestFactory.OFFSET_PARAMETER, "0");
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(pagedRequest, ProjectView.class);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());

        int i = 0;
        int listCount = allPagesResponse.getItems().size();
        while (i < listCount) {
            String myTestUrl = allPagesResponse.getItems().get(i).getHref().string();
            System.out.printf("Running test %d of %d testing %s%n", i, listCount - 1, myTestUrl);
            
            Predicate<ProjectView> predicate = httpUrl -> myTestUrl.equalsIgnoreCase(httpUrl.getHref().string());
            BlackDuckPageResponse<ProjectView> matchedResponse = blackDuckResponsesTransformer.getMatchingResponse(pagedRequest, ProjectView.class, predicate);

            assertEquals(69, matchedResponse.getTotalCount());
            assertEquals(1, matchedResponse.getItems().size());
            assertEquals(myTestUrl, matchedResponse.getItems().get(0).getHref().string());
            i++;
        }

    }

    @Test
    public void testGettingAllOnePageTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_all_on_one_page.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 100);

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new MediaTypeDiscovery(), new Request.Builder());
        requestBuilder
            .url(new HttpUrl("https://blackduckserver.com/api/projects"))
            .addQueryParameter(RequestFactory.LIMIT_PARAMETER, "100")
            .addQueryParameter(RequestFactory.OFFSET_PARAMETER, "0");
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(pagedRequest, ProjectView.class);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingAllMultiplePagesTotal() throws IntegrationException, IOException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_page_1_of_4.json");
        offsetsToResults.put("20", "projectViews_page_2_of_4.json");
        offsetsToResults.put("40", "projectViews_page_3_of_4.json");
        offsetsToResults.put("60", "projectViews_page_4_of_4.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 20);

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new MediaTypeDiscovery(), new Request.Builder());
        requestBuilder
            .url(new HttpUrl("https://blackduckserver.com/api/projects"))
            .addQueryParameter(RequestFactory.LIMIT_PARAMETER, "20")
            .addQueryParameter(RequestFactory.OFFSET_PARAMETER, "0");
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getAllResponses(pagedRequest, ProjectView.class);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingOnePageOnePageTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_all_on_one_page.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 100);

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new MediaTypeDiscovery(), new Request.Builder());
        requestBuilder
            .url(new HttpUrl("https://blackduckserver.com/api/projects"))
            .addQueryParameter(RequestFactory.LIMIT_PARAMETER, "100")
            .addQueryParameter(RequestFactory.OFFSET_PARAMETER, "0");
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, ProjectView.class);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(69, allPagesResponse.getItems().size());
    }

    @Test
    public void testGettingOnePageMultiplePagesTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projectViews_page_1_of_4.json");
        offsetsToResults.put("20", "projectViews_page_2_of_4.json");
        offsetsToResults.put("40", "projectViews_page_3_of_4.json");
        offsetsToResults.put("60", "projectViews_page_4_of_4.json");

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        mockClientBehavior(blackDuckHttpClient, offsetsToResults, 20);

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            new PrintStreamIntLogger(System.out, LogLevel.INFO));

        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new MediaTypeDiscovery(), new Request.Builder());
        requestBuilder
            .url(new HttpUrl("https://blackduckserver.com/api/projects"))
            .addQueryParameter(RequestFactory.LIMIT_PARAMETER, "20")
            .addQueryParameter(RequestFactory.OFFSET_PARAMETER, "0");
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        BlackDuckPageResponse<ProjectView> allPagesResponse = blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, ProjectView.class);
        assertEquals(69, allPagesResponse.getTotalCount());
        assertEquals(20, allPagesResponse.getItems().size());
    }

    private void mockClientBehavior(BlackDuckHttpClient blackDuckHttpClient, Map<String, String> offsetsToResults, int limit) throws IOException, IntegrationException {
        for (Map.Entry<String, String> entry : offsetsToResults.entrySet()) {
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getContentString()).thenReturn(getText(entry.getValue()));

            ArgumentMatcher<Request> argRequest = createRequestMatcher(new HttpUrl("https://blackduckserver.com/api/projects"), Integer.parseInt(entry.getKey()), limit);
            Mockito.when(blackDuckHttpClient.execute(Mockito.argThat(argRequest))).thenReturn(response);
        }
    }

    private ArgumentMatcher<Request> createRequestMatcher(HttpUrl url, int offset, int limit) {
        return new ArgumentMatcher<Request>() {
            @Override
            public boolean matches(Request request) {
                if (null != request && request.getUrl().equals(url)) {
                    String requestOffset = request.getQueryParameters().get(RequestFactory.OFFSET_PARAMETER).stream().findFirst().get();
                    return requestOffset.equals(Integer.toString(offset));
                }
                return false;
            }
        };
    }

    private String getText(String resourceName) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/BlackDuckResponsesTransformer/" + resourceName), StandardCharsets.UTF_8);
    }

}
