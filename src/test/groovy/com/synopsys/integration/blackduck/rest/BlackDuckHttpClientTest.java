package com.synopsys.integration.blackduck.rest;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TimingExtension.class)
public class BlackDuckHttpClientTest {
    @Test
    public void testRedirectMovedPermanently() throws IntegrationException {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        AuthenticationSupport authenticationSupport = new AuthenticationSupport();
        Gson gson = new Gson();

//        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
//        credentialsBuilder.setUsernameAndPassword("username", "password");
//        Credentials credentials = credentialsBuilder.build();
//
//        CredentialsBlackDuckHttpClient credentialsHttpClient = new CredentialsBlackDuckHttpClient(logger, 120, false, ProxyInfo.NO_PROXY_INFO, "http://localhost:8080", authenticationSupport, credentials);
//        ApiTokenBlackDuckHttpClient apiTokenHttpClient = new ApiTokenBlackDuckHttpClient(logger, 120, false, ProxyInfo.NO_PROXY_INFO, "http://localhost:8080", gson, authenticationSupport, "apitoken");
//
//        Request req = new Request.Builder("http://localhost:8080").build();
//        Response credentialsResponse = credentialsHttpClient.execute(req);
//        System.out.println("**" + credentialsResponse.getContentString() + "**");
//
//        Response tokenResponse = apiTokenHttpClient.execute(req);
//        System.out.println("**" + tokenResponse.getContentString() + "**");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl("http://localhost:8080/bd/");
        //blackDuckServerConfigBuilder.setUrl("https://int-bdstarlabs.dc1.lan");
        blackDuckServerConfigBuilder.setApiToken("NjliOTU0N2ItOTJkMC00YjdmLTgwNTItMjJiZmE4ZWYzNGY4OjZjNzJhNzJhLWM5ZjYtNGQyOC05YzY0LTc4MGQzYzA1MmY1MQ==");
//        blackDuckServerConfigBuilder.setUsername("sysadmin");
//        blackDuckServerConfigBuilder.setPassword("blackduck");
        blackDuckServerConfigBuilder.setProxyInfo(ProxyInfo.NO_PROXY_INFO);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();

        List<ProjectView> allProjects = projectService.getAllProjects();
        assertTrue(allProjects.size() > 0);
        allProjects.stream().map(ProjectView::getName).forEach(System.out::println);
    }

    public void testConnecting() throws IntegrationException {
/**
 IntHttpClient intHttpClient = new IntHttpClient()
 public IntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {

 */
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        IntHttpClient intHttpClient = new IntHttpClient(logger, 120, false, ProxyInfo.NO_PROXY_INFO);
        Request.Builder requestBuilder = new Request.Builder(new HttpUrl("http://www.blackducksoftware.com"));
        Response response = intHttpClient.execute(requestBuilder.build());
        System.out.println(response.getContentString());
    }

}
