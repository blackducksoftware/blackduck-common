package com.synopsys.integration.blackduck.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class SignatureScannerClientProxyTest {

    private static final BufferedIntLogger LOGGER = new BufferedIntLogger();
    private static final BlackDuckRequestFactory BLACK_DUCK_REQUEST_FACTORY = new BlackDuckRequestFactory();

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
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, 10, false, ProxyInfo.NO_PROXY_INFO);
        HttpUrl httpsServer = new HttpUrl("http://127.0.0.1:" + MOCK_SERVER.getPort());
        Request request = BLACK_DUCK_REQUEST_FACTORY.createCommonGetRequest(httpsServer);
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
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, 10, false, PROXY_INFO);
        HttpUrl httpsServer = new HttpUrl("http://127.0.0.1:" + MOCK_SERVER.getPort());
        Request request = BLACK_DUCK_REQUEST_FACTORY.createCommonGetRequest(httpsServer);
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
