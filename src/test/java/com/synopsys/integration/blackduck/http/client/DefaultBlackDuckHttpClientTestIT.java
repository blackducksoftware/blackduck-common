package com.synopsys.integration.blackduck.http.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

import okhttp3.mockwebserver.MockWebServer;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class DefaultBlackDuckHttpClientTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    private static final String API_TOKEN_NAME = "blackDuckHttpClientTest";
    private static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens");
    private static final BlackDuckPathMultipleResponses<ApiTokenView> API_TOKEN_LINK_RESPONSE = new BlackDuckPathMultipleResponses<>(DefaultBlackDuckHttpClientTestIT.API_TOKEN_LINK, ApiTokenView.class);

    private final BlackDuckRequestFactory blackDuckRequestFactory = new BlackDuckRequestFactory();
    private final HttpUrl blackDuckUrl = INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl();
    private final String username = INT_HTTP_CLIENT_TEST_HELPER.getTestUsername();
    private final String password = INT_HTTP_CLIENT_TEST_HELPER.getTestPassword();
    private final BlackDuckApiClient blackDuckApiClient = INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory().getBlackDuckService();

    public DefaultBlackDuckHttpClientTestIT() throws IntegrationException {
    }

    @Test
    public void testCredentials() throws IntegrationException, IOException {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(blackDuckUrl);
        builder.setUsername(username);
        builder.setPassword(password);

        BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());
        DefaultBlackDuckHttpClient defaultBlackDuckHttpClient = validConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = defaultBlackDuckHttpClient.attemptAuthentication()) {
            assertTrue(response.isStatusCodeSuccess());
            assertFalse(response.isStatusCodeError());
        }
        testRequestRequiringAuthentication(validConfig);

        builder.setUrl("https://www.google.com");
        builder.setUsername(username);
        builder.setPassword(password);
        BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());
        defaultBlackDuckHttpClient = invalidUrlConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = defaultBlackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeSuccess());
            assertTrue(response.isStatusCodeError());
        }

        builder.setUrl(blackDuckUrl);
        builder.setUsername(username);
        builder.setPassword("this is not the password");
        BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
        defaultBlackDuckHttpClient = invalidPasswordConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = defaultBlackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeSuccess());
            assertTrue(response.isStatusCodeError());
        }
    }

    @Test
    public void testApiToken() throws IntegrationException, IOException {
        deleteByName(DefaultBlackDuckHttpClientTestIT.API_TOKEN_NAME);
        ApiTokenView apiTokenView = getApiToken(DefaultBlackDuckHttpClientTestIT.API_TOKEN_NAME);

        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(blackDuckUrl);
        builder.setApiToken(apiTokenView.token);

        BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());
        testRequestRequiringAuthentication(validConfig);

        builder.setUrl("https://www.google.com");
        builder.setApiToken(apiTokenView.token);
        BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());

        builder.setUrl(blackDuckUrl);
        builder.setApiToken("for serious, this is not an api token");
        BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
    }

    @Test
    public void testBlackDuckRedirectWithCredentials() throws IntegrationException, IOException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUsername(username);
        blackDuckServerConfigBuilder.setPassword(password);

        testRedirect(blackDuckServerConfigBuilder);
    }

    @Test
    public void testBlackDuckRedirectWithApiToken() throws IntegrationException, IOException {
        String tokenName = "redirect-test";
        ApiTokenView apiTokenView = getApiToken(tokenName);
        try {
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            blackDuckServerConfigBuilder.setApiToken(apiTokenView.token);

            testRedirect(blackDuckServerConfigBuilder);
        } finally {
            deleteByName(tokenName);
        }
    }

    private void testRedirect(BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException, IOException {
        blackDuckServerConfigBuilder.setTrustCert(true);

        MockWebServer redirectingServer = new MockWebServer();
        MockWebServerUtil.setupRedirecting(redirectingServer, blackDuckUrl.string());
        redirectingServer.start();
        String redirectingServerUrl = redirectingServer.url("/").toString();

        blackDuckServerConfigBuilder.setUrl(redirectingServerUrl);
        blackDuckServerConfigBuilder.setProxyInfo(ProxyInfo.NO_PROXY_INFO);

        testRequestRequiringAuthentication(blackDuckServerConfigBuilder.build());
    }

    private void testRequestRequiringAuthentication(BlackDuckServerConfig blackDuckServerConfig) throws IntegrationException {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
        UserView currentUser = blackDuckApiClient.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        assertNotNull(currentUser);
        assertEquals(username, currentUser.getUserName());
    }

    @Test
    @Disabled
    public void testProvidedApiToken() {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setUrl("https://int-hub02.dc1.lan");
        builder.setApiToken("MTYzZjkxMjAtMDBmMi00NTk4LWJmNjEtZmYzYWIyMmEwNWE2OjJkYjQwZDJiLTAzYWQtNGZiOC05ZTJjLTY3MWQyZTcwNWIzOQ==");
        builder.setTrustCert(true);
        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertTrue(blackDuckServerConfig.canConnect());
    }

    // WARNING!!!!
    // ******************************
    // These API token methods are NOT(!!!) intended for production use! They are used here to enable automated testing
    // but should not serve as an example of how to create API tokens in Black Duck.
    // ******************************
    // WARNING!!!!
    private ApiTokenView getApiToken(String tokenName) throws IntegrationException, IOException {
        HttpUrl blackDuckServerUrl = blackDuckUrl;
        HttpUrl createApiTokenUrl = blackDuckServerUrl.appendRelativeUrl(DefaultBlackDuckHttpClientTestIT.API_TOKEN_LINK.getPath());

        ApiTokenRequest apiTokenRequest = new ApiTokenRequest();
        apiTokenRequest.name = tokenName;
        apiTokenRequest.scopes.add("read");
        apiTokenRequest.scopes.add("write");

        ApiTokenView apiTokenView;
        String json = blackDuckApiClient.convertToJson(apiTokenRequest);
        Request request = blackDuckRequestFactory.createCommonPostRequestBuilder(createApiTokenUrl, json).build();
        try (Response response = blackDuckApiClient.execute(request)) {
            apiTokenView = blackDuckApiClient.transformResponse(response, ApiTokenView.class);
        }

        return apiTokenView;
    }

    private void deleteByName(String name) throws IntegrationException {
        List<ApiTokenView> apiTokens = blackDuckApiClient.getAllResponses(DefaultBlackDuckHttpClientTestIT.API_TOKEN_LINK_RESPONSE);
        for (ApiTokenView apiTokenView : apiTokens) {
            if (apiTokenView.name.equals(name)) {
                blackDuckApiClient.delete(apiTokenView);
            }
        }
    }

    private class ApiTokenRequest extends BlackDuckComponent {
        public String name;
        public String description;
        public List<String> scopes = new ArrayList<>();
    }

    private class ApiTokenView extends BlackDuckView {
        public String name;
        public String description;
        public List<String> scopes = new ArrayList<>();
        public String token;
    }

}
