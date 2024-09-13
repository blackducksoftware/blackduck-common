package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.core.BlackDuckComponent;
import com.blackduck.integration.blackduck.api.core.BlackDuckPath;
import com.blackduck.integration.blackduck.api.core.BlackDuckView;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.UserService;
import com.blackduck.integration.blackduck.service.request.BlackDuckSingleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.log.SilentIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.body.BodyContentConverter;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.Response;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckHttpClientTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    private static final String API_TOKEN_NAME = "blackDuckHttpClientTest";
    private static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens", ApiTokenView.class, true);

    private final BlackDuckServicesFactory blackDuckServicesFactory = INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
    private final BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
    private final HttpUrl blackDuckUrl = INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl();
    private final String username = INT_HTTP_CLIENT_TEST_HELPER.getTestUsername();
    private final String password = INT_HTTP_CLIENT_TEST_HELPER.getTestPassword();
    private final UserService userService = blackDuckServicesFactory.createUserService();

    public BlackDuckHttpClientTestIT() throws IntegrationException {
    }

    @Test
    public void testCredentials() throws IntegrationException, IOException {
        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newCredentialsBuilder();
        builder.setTrustCert(true);
        builder.setUrl(blackDuckUrl);
        builder.setUsername(username);
        builder.setPassword(password);

        BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());
        BlackDuckHttpClient blackDuckHttpClient = validConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertTrue(response.isStatusCodeSuccess());
            assertFalse(response.isStatusCodeError());
        }
        testRequestRequiringAuthentication(validConfig);

        builder.setUrl("https://www.google.com");
        builder.setUsername(username);
        builder.setPassword(password);
        BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());
        blackDuckHttpClient = invalidUrlConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeSuccess());
            assertTrue(response.isStatusCodeError());
        }

        builder.setUrl(blackDuckUrl);
        builder.setUsername(username);
        builder.setPassword("this is not the password");
        BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
        blackDuckHttpClient = invalidPasswordConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeSuccess());
            assertTrue(response.isStatusCodeError());
        }
    }

    @Test
    public void testApiToken() throws IntegrationException, IOException {
        deleteByName(BlackDuckHttpClientTestIT.API_TOKEN_NAME);
        ApiTokenView apiTokenView = getApiToken(BlackDuckHttpClientTestIT.API_TOKEN_NAME);

        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newApiTokenBuilder();
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
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUsername(username);
        blackDuckServerConfigBuilder.setPassword(password);

        testRedirect(blackDuckServerConfigBuilder);
    }

    @Test
    public void testBlackDuckRedirectWithApiToken() throws IntegrationException, IOException {
        String tokenName = "redirect-test";
        ApiTokenView apiTokenView = getApiToken(tokenName);
        try {
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
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
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        UserView currentUser = userService.findCurrentUser();
        assertNotNull(currentUser);
        assertEquals(username, currentUser.getUserName());
    }

    @Test
    @Disabled
    public void testProvidedApiToken() {
        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newApiTokenBuilder();
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
        UrlSingleResponse<ApiTokenView> tokenResponse = apiDiscovery.metaSingleResponse(BlackDuckHttpClientTestIT.API_TOKEN_LINK);

        ApiTokenRequest apiTokenRequest = new ApiTokenRequest();
        apiTokenRequest.name = tokenName;
        apiTokenRequest.scopes.add("read");
        apiTokenRequest.scopes.add("write");

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
            .postObject(apiTokenRequest, BodyContentConverter.DEFAULT);
        BlackDuckSingleRequest<ApiTokenView> requestSingle = blackDuckRequestBuilder.buildBlackDuckRequest(tokenResponse);

        return blackDuckApiClient.getResponse(requestSingle);
    }

    private void deleteByName(String name) throws IntegrationException {
        List<ApiTokenView> apiTokens = blackDuckApiClient.getAllResponses(apiDiscovery.metaMultipleResponses(API_TOKEN_LINK));
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
