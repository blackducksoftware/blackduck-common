package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

public class SignatureScannerClientProxyTest {
    private static final BufferedIntLogger LOGGER = new BufferedIntLogger();
    private static final Gson gson = new Gson();

    private static final int MOCK_SERVER_STATUS_CODE = 500;
    private static final String MOCK_SERVER_HEADER_KEY = "MockServerKey";
    private static final String MOCK_SERVER_HEADER_VALUE = "MockServerHeader";
    private static final String MOCK_SERVER_BODY = "Mock Server Body";
    private static final int PROXY_SERVER_STATUS_CODE = 404;
    private static final String PROXY_SERVER_HEADER_KEY = "ProxyServerKey";
    private static final String PROXY_SERVER_HEADER_VALUE = "ProxyServerHeader";
    private static final String PROXY_SERVER_BODY = "Proxy Server Body";

    private static ClientAndServer PROXY_SERVER;
    private static ClientAndServer MOCK_SERVER;
    private static ProxyInfo PROXY_INFO;

    @BeforeClass
    public static void setUp() {
        ConfigurationProperties.logLevel("OFF");
        PROXY_SERVER = startClientAndServer();
        MOCK_SERVER = startClientAndServer();
        configureServerResponse(MOCK_SERVER, MOCK_SERVER_STATUS_CODE, MOCK_SERVER_HEADER_KEY, MOCK_SERVER_HEADER_VALUE, MOCK_SERVER_BODY);
        configureServerResponse(PROXY_SERVER, PROXY_SERVER_STATUS_CODE, PROXY_SERVER_HEADER_KEY, PROXY_SERVER_HEADER_VALUE, PROXY_SERVER_BODY);

        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setPort(PROXY_SERVER.getPort());
        proxyInfoBuilder.setHost("127.0.0.1");
        PROXY_INFO = proxyInfoBuilder.build();
    }

    @AfterClass
    public static void tearDown() {
        stopQuietly(MOCK_SERVER);
        stopQuietly(PROXY_SERVER);
    }

    @Test
    public void noProxyTest() throws IntegrationException {
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, gson, 10, false, ProxyInfo.NO_PROXY_INFO);
        HttpUrl httpsServer = new HttpUrl("http://127.0.0.1:" + MOCK_SERVER.getPort());
        Request request = new BlackDuckRequestBuilder().commonGet().url(httpsServer).build();
        Response response = signatureScannerClient.execute(request);
        Map<String, String> headers = response.getHeaders();

        assertEquals(MOCK_SERVER_STATUS_CODE, response.getStatusCode());
        assertTrue(headers.containsKey(MOCK_SERVER_HEADER_KEY), String.format("Response headers do not contain key %s and should", MOCK_SERVER_HEADER_KEY));
        assertEquals(MOCK_SERVER_HEADER_VALUE, headers.get(MOCK_SERVER_HEADER_KEY), String.format("Response headers do not contain value %s for key %s and should", MOCK_SERVER_HEADER_VALUE, MOCK_SERVER_HEADER_KEY));
        assertFalse(headers.containsKey(PROXY_SERVER_HEADER_KEY), String.format("Response headers should NOT contain key %s and does", PROXY_SERVER_HEADER_KEY));
        assertEquals(MOCK_SERVER_BODY, response.getContentString());
    }

    @Test
    public void withProxyTest() throws IntegrationException {
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, gson, 10, false, PROXY_INFO);
        HttpUrl httpsServer = new HttpUrl("http://127.0.0.1:" + MOCK_SERVER.getPort());
        Request request = new BlackDuckRequestBuilder().commonGet().url(httpsServer).build();
        Response response = signatureScannerClient.execute(request);
        Map<String, String> headers = response.getHeaders();

        assertEquals(PROXY_SERVER_STATUS_CODE, response.getStatusCode());
        assertTrue(headers.containsKey(PROXY_SERVER_HEADER_KEY), String.format("Response headers do not contain key %s and should", PROXY_SERVER_HEADER_KEY));
        assertEquals(PROXY_SERVER_HEADER_VALUE, headers.get(PROXY_SERVER_HEADER_KEY), String.format("Response headers do not contain value %s for key %s and should", PROXY_SERVER_HEADER_VALUE, PROXY_SERVER_HEADER_KEY));
        assertFalse(headers.containsKey(MOCK_SERVER_HEADER_KEY), String.format("Response headers should NOT contain key %s and does", MOCK_SERVER_HEADER_KEY));
        assertEquals(PROXY_SERVER_BODY, response.getContentString());
    }

    private static void configureServerResponse(ClientAndServer server, Integer responseCode, String headerKey, String headerValue, String body) {
        server.when(
            request()
                .withMethod("GET")
                .withPath("/"))
            .respond(
                response()
                    .withStatusCode(responseCode)
                    .withHeaders(
                        new Header(headerKey, headerValue)
                    )
                    .withBody(body)
                    .withDelay(TimeUnit.SECONDS, 1)
            );
    }

}
