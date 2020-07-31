package com.synopsys.integration.blackduck.http.client;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TimingExtension.class)
public class BlackDuckRedirectStrategyTest {
    @Test
    public void testSimpleRedirect() throws IOException, IntegrationException, InterruptedException {
        // we need two servers - one to redirect to the other
        MockWebServer redirectingServer = new MockWebServer();
        MockWebServer destinationServer = new MockWebServer();

        String destinationUrl = startDestinationServer(destinationServer);
        String redirectingServerUrl = startRedirectingServer(redirectingServer, destinationUrl);

        IntLogger testLogger = new BufferedIntLogger();
        IntHttpClient redirectingClient = new IntHttpClient(testLogger, 120, false, ProxyInfo.NO_PROXY_INFO) {
            @Override
            public void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
                super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
                httpClientBuilder.setRedirectStrategy(new BlackDuckRedirectStrategy());
            }
        };

        Request requestToRedirectingServer = new Request.Builder(new com.synopsys.integration.rest.HttpUrl(redirectingServerUrl))
                .method(HttpMethod.POST)
                .bodyContent(new StringBodyContent("the initial request payload"))
                .build();
        Response response = redirectingClient.execute(requestToRedirectingServer);

        assertEquals("the final response body", response.getContentString());

        RecordedRequest initialRequest = redirectingServer.takeRequest();
        RecordedRequest redirectedRequest = destinationServer.takeRequest();

        assertEquals(initialRequest.getPath(), redirectedRequest.getPath());
        assertEquals("/donkeynoodle", redirectedRequest.getPath());

        assertEquals(initialRequest.getBody(), redirectedRequest.getBody());
        assertEquals("the initial request payload", redirectedRequest.getBody().readString(StandardCharsets.UTF_8));

        shutdownServers(redirectingServer, destinationServer);
    }

    private String startDestinationServer(MockWebServer destinationServer) throws IOException {
        destinationServer.enqueue(new MockResponse().setBody("the final response body"));
        destinationServer.start();
        return destinationServer.url("/").toString();
    }

    private String startRedirectingServer(MockWebServer redirectingServer, String destinationUrl) throws IOException {
        MockWebServerUtil.setupRedirecting(redirectingServer, destinationUrl);
        redirectingServer.start();
        return redirectingServer.url("/donkeynoodle").toString();
    }

    private void shutdownServers(MockWebServer redirectingServer, MockWebServer destinationServer) throws IOException {
        redirectingServer.shutdown();
        destinationServer.shutdown();
    }

}
