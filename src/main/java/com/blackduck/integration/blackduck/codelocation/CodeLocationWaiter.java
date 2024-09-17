/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;
import com.blackduck.integration.wait.ResilientJobConfig;
import com.blackduck.integration.wait.ResilientJobExecutor;
import com.blackduck.integration.wait.tracker.WaitIntervalTracker;
import com.blackduck.integration.wait.tracker.WaitIntervalTrackerFactory;

import java.util.Set;

public class CodeLocationWaiter {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    public CodeLocationWaiter(IntLogger logger, BlackDuckApiClient blackDuckApiClient, ProjectService projectService, NotificationService notificationService) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.projectService = projectService;
        this.notificationService = notificationService;
    }

    public CodeLocationWaitResult checkCodeLocationsAddedToBom(UserView userView, NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount,
                                                               long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        logger.debug("Expected notification count " + expectedNotificationCount);
        logger.debug("Expected code locations:");
        codeLocationNames.forEach(codeLocation -> logger.debug(String.format("  Code Location -> %s", codeLocation)));
        logger.debug("");

        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(timeoutInSeconds, waitIntervalInSeconds);
        ResilientJobConfig jobConfig = new ResilientJobConfig(logger, notificationTaskRange.getTaskStartTime(), waitIntervalTracker);


        CodeLocationWaitJob codeLocationWaitJob = new CodeLocationWaitJob(logger, projectService, notificationService, userView, notificationTaskRange, projectAndVersion, codeLocationNames,
            expectedNotificationCount, blackDuckApiClient);
        ResilientJobExecutor resilientJobExecutor = new ResilientJobExecutor(jobConfig);
        return resilientJobExecutor.executeJob(codeLocationWaitJob);
    }

}
