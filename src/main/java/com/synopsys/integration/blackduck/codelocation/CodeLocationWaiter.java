/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.ResilientJobExecutor;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

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
