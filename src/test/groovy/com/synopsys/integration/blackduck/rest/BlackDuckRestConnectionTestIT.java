package com.synopsys.integration.blackduck.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
public class BlackDuckRestConnectionTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final String API_TOKEN_NAME = "blackDuckRestConnectionTest";
    private static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens");
    private static final BlackDuckPathMultipleResponses<ApiTokenView> API_TOKEN_LINK_RESPONSE = new BlackDuckPathMultipleResponses<>(API_TOKEN_LINK, ApiTokenView.class);

    @Test
    public void testCredentials() throws IntegrationException, IOException {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(restConnectionTestHelper.getIntegrationBlackDuckServerUrl());
        builder.setUsername(restConnectionTestHelper.getTestUsername());
        builder.setPassword(restConnectionTestHelper.getTestPassword());

        final BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());
        BlackDuckRestConnection blackDuckRestConnection = validConfig.createRestConnection(new SilentIntLogger());
        try (Response response = blackDuckRestConnection.attemptAuthentication()) {
            assertTrue(response.isStatusCodeOkay());
            assertFalse(response.isStatusCodeError());
        }

        builder.setUrl("https://www.google.com");
        builder.setUsername(restConnectionTestHelper.getTestUsername());
        builder.setPassword(restConnectionTestHelper.getTestPassword());
        final BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());
        blackDuckRestConnection = invalidUrlConfig.createRestConnection(new SilentIntLogger());
        try (Response response = blackDuckRestConnection.attemptAuthentication()) {
            assertFalse(response.isStatusCodeOkay());
            assertTrue(response.isStatusCodeError());
        }

        builder.setUrl(restConnectionTestHelper.getIntegrationBlackDuckServerUrl());
        builder.setUsername(restConnectionTestHelper.getTestUsername());
        builder.setPassword("this is not the password");
        final BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
        blackDuckRestConnection = invalidPasswordConfig.createRestConnection(new SilentIntLogger());
        try (Response response = blackDuckRestConnection.attemptAuthentication()) {
            assertFalse(response.isStatusCodeOkay());
            assertTrue(response.isStatusCodeError());
        }
    }

    @Test
    public void testApiToken() throws IntegrationException, IOException {
        deleteByName(API_TOKEN_NAME);
        final ApiTokenView apiTokenView = getApiToken(API_TOKEN_NAME);

        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(restConnectionTestHelper.getIntegrationBlackDuckServerUrl());
        builder.setApiToken(apiTokenView.token);

        final BlackDuckServerConfig validConfig = builder.build();
        assertTrue(validConfig.canConnect());

        builder.setUrl("https://www.google.com");
        builder.setApiToken(apiTokenView.token);
        final BlackDuckServerConfig invalidUrlConfig = builder.build();
        assertFalse(invalidUrlConfig.canConnect());

        builder.setUrl(restConnectionTestHelper.getIntegrationBlackDuckServerUrl());
        builder.setApiToken("for serious, this is not an api token");
        final BlackDuckServerConfig invalidPasswordConfig = builder.build();
        assertFalse(invalidPasswordConfig.canConnect());
    }

    // WARNING!!!!
    // ******************************
    // These API token methods are NOT(!!!) intended for production use! They are used here to enable automated testing
    // but should not serve as an example of how to create API tokens in Black Duck.
    // ******************************
    // WARNING!!!!
    private ApiTokenView getApiToken(final String tokenName) throws IntegrationException, IOException {
        final BlackDuckService blackDuckService = restConnectionTestHelper.createBlackDuckServicesFactory().createBlackDuckService();

        final URL blackDuckServerUrl = new URL(restConnectionTestHelper.getIntegrationBlackDuckServerUrl());
        final String createApiTokenUrl = new URL(blackDuckServerUrl, API_TOKEN_LINK.getPath()).toString();

        final ApiTokenRequest apiTokenRequest = new ApiTokenRequest();
        apiTokenRequest.name = tokenName;
        apiTokenRequest.scopes.add("read");
        apiTokenRequest.scopes.add("write");

        final ApiTokenView apiTokenView;
        final String json = blackDuckService.convertToJson(apiTokenRequest);
        final Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(createApiTokenUrl).build();
        try (Response response = blackDuckService.execute(request)) {
            apiTokenView = blackDuckService.transformResponse(response, ApiTokenView.class);
        }

        return apiTokenView;
    }

    private void deleteByName(final String name) throws IntegrationException {
        final BlackDuckService blackDuckService = restConnectionTestHelper.createBlackDuckServicesFactory().createBlackDuckService();
        final List<ApiTokenView> apiTokens = blackDuckService.getAllResponses(API_TOKEN_LINK_RESPONSE);
        for (final ApiTokenView apiTokenView : apiTokens) {
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
