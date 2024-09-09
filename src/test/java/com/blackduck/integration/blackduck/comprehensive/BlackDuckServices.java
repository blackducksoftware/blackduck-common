package com.blackduck.integration.blackduck.comprehensive;

import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.codelocation.CodeLocationCreationService;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.blackduck.integration.blackduck.service.dataservice.CodeLocationService;
import com.blackduck.integration.blackduck.service.dataservice.ComponentService;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.dataservice.PolicyRuleService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectBomService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectUsersService;
import com.blackduck.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class BlackDuckServices {
    public IntLogger logger;
    public BlackDuckServicesFactory blackDuckServicesFactory;
    public ApiDiscovery apiDiscovery;
    public BlackDuckServerConfig blackDuckServerConfig;
    public ProjectService projectService;
    public ProjectUsersService projectUsersService;
    public ProjectBomService projectBomService;
    public CodeLocationService codeLocationService;
    public BlackDuckApiClient blackDuckApiClient;
    public ComponentService componentService;
    public PolicyRuleService policyRuleService;
    public CodeLocationCreationService codeLocationCreationService;
    public NotificationService notificationService;
    public UserService userService;
    public BlackDuckRegistrationService blackDuckRegistrationService;

    public BlackDuckServices(IntHttpClientTestHelper intHttpClientTestHelper) throws IntegrationException {
        logger = new PrintStreamIntLogger(System.out, LogLevel.OFF);
        blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory(logger);
        apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
        blackDuckServerConfig = intHttpClientTestHelper.getBlackDuckServerConfig();
        projectService = blackDuckServicesFactory.createProjectService();
        projectUsersService = blackDuckServicesFactory.createProjectUsersService();
        projectBomService = blackDuckServicesFactory.createProjectBomService();
        codeLocationService = blackDuckServicesFactory.createCodeLocationService();
        blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        componentService = blackDuckServicesFactory.createComponentService();
        policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        notificationService = blackDuckServicesFactory.createNotificationService();
        userService = blackDuckServicesFactory.createUserService();
        blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
    }

}