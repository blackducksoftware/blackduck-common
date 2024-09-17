package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.body.BodyContentConverter;
import com.blackduck.integration.rest.body.StringBodyContent;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
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
    private final Gson gson = new Gson();

    @Test
    public void testSimpleRedirect() throws IOException, IntegrationException, InterruptedException {
        // we need two servers - one to redirect to the other
        MockWebServer redirectingServer = new MockWebServer();
        MockWebServer destinationServer = new MockWebServer();

        String destinationUrl = startDestinationServer(destinationServer);
        String redirectingServerUrl = startRedirectingServer(redirectingServer, destinationUrl);

        IntLogger testLogger = new BufferedIntLogger();
        IntHttpClient redirectingClient = new IntHttpClient(testLogger, gson, 120, false, ProxyInfo.NO_PROXY_INFO) {
            @Override
            public void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
                super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
                httpClientBuilder.setRedirectStrategy(new BlackDuckRedirectStrategy());
            }
        };

        Request requestToRedirectingServer = new Request.Builder(new HttpUrl(redirectingServerUrl))
                                                 .method(HttpMethod.POST)
                                                 .bodyContent(new StringBodyContent("the initial request payload", BodyContentConverter.DEFAULT))
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
