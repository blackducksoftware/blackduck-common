package com.synopsys.integration.blackduck.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

@Tag("integration")
public class BlackDuckHttpClientTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    private static final String API_TOKEN_NAME = "blackDuckHttpClientTest";
    private static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens");
    private static final BlackDuckPathMultipleResponses<ApiTokenView> API_TOKEN_LINK_RESPONSE = new BlackDuckPathMultipleResponses<>(BlackDuckHttpClientTestIT.API_TOKEN_LINK, ApiTokenView.class);

    @Test
    public void testCredentials() throws IntegrationException, IOException {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl());
        builder.setUsername(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        builder.setPassword(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestPassword());

        BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());
        BlackDuckHttpClient blackDuckHttpClient = validConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertTrue(response.isStatusCodeOkay());
            assertFalse(response.isStatusCodeError());
        }

        builder.setUrl("https://www.google.com");
        builder.setUsername(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        builder.setPassword(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestPassword());
        BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());
        blackDuckHttpClient = invalidUrlConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeOkay());
            assertTrue(response.isStatusCodeError());
        }

        builder.setUrl(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl());
        builder.setUsername(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        builder.setPassword("this is not the password");
        BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
        blackDuckHttpClient = invalidPasswordConfig.createBlackDuckHttpClient(new SilentIntLogger());
        try (Response response = blackDuckHttpClient.attemptAuthentication()) {
            assertFalse(response.isStatusCodeOkay());
            assertTrue(response.isStatusCodeError());
        }
    }

    @Test
    public void testApiToken() throws IntegrationException, IOException {
        deleteByName(BlackDuckHttpClientTestIT.API_TOKEN_NAME);
        ApiTokenView apiTokenView = getApiToken(BlackDuckHttpClientTestIT.API_TOKEN_NAME);

        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl());
        builder.setApiToken(apiTokenView.token);

        BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());

        builder.setUrl("https://www.google.com");
        builder.setApiToken(apiTokenView.token);
        BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());

        builder.setUrl(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl());
        builder.setApiToken("for serious, this is not an api token");
        BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
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
        BlackDuckService blackDuckService = BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory().createBlackDuckService();

        URL blackDuckServerUrl = new URL(BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl());
        String createApiTokenUrl = new URL(blackDuckServerUrl, BlackDuckHttpClientTestIT.API_TOKEN_LINK.getPath()).toString();

        ApiTokenRequest apiTokenRequest = new ApiTokenRequest();
        apiTokenRequest.name = tokenName;
        apiTokenRequest.scopes.add("read");
        apiTokenRequest.scopes.add("write");

        ApiTokenView apiTokenView;
        String json = blackDuckService.convertToJson(apiTokenRequest);
        Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(createApiTokenUrl).build();
        try (Response response = blackDuckService.execute(request)) {
            apiTokenView = blackDuckService.transformResponse(response, ApiTokenView.class);
        }

        return apiTokenView;
    }

    private void deleteByName(String name) throws IntegrationException {
        BlackDuckService blackDuckService = BlackDuckHttpClientTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory().createBlackDuckService();
        List<ApiTokenView> apiTokens = blackDuckService.getAllResponses(BlackDuckHttpClientTestIT.API_TOKEN_LINK_RESPONSE);
        for (ApiTokenView apiTokenView : apiTokens) {
            if (apiTokenView.name.equals(name)) {
                blackDuckService.delete(apiTokenView);
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
