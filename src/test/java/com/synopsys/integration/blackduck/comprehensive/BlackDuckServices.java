package com.synopsys.integration.blackduck.comprehensive;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class BlackDuckServices {
    public IntLogger logger;
    public BlackDuckServicesFactory blackDuckServicesFactory;
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

    public BlackDuckServices(IntHttpClientTestHelper intHttpClientTestHelper) throws IntegrationException {
        logger = new PrintStreamIntLogger(System.out, LogLevel.OFF);
        blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory(logger);
        blackDuckServerConfig = intHttpClientTestHelper.getBlackDuckServerConfig();
        projectService = blackDuckServicesFactory.createProjectService();
        projectUsersService = blackDuckServicesFactory.createProjectUsersService();
        projectBomService = blackDuckServicesFactory.createProjectBomService();
        codeLocationService = blackDuckServicesFactory.createCodeLocationService();
        blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
        componentService = blackDuckServicesFactory.createComponentService();
        policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        notificationService = blackDuckServicesFactory.createNotificationService();
    }

}