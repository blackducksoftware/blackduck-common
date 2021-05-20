/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.Set;
import java.util.function.Function;

import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobCompleter;
import com.synopsys.integration.wait.WaitJobConfig;
import com.synopsys.integration.wait.WaitJobInitializer;
import com.synopsys.integration.wait.WaitJobTask;
import com.synopsys.integration.wait.WaitJobTimeoutHandler;

public class CodeLocationWaiter {
    public static final Function<Long, String> ERROR_MESSAGE =
        (timeoutInSeconds) -> String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds);

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
        WaitJobInitializer initializer = () -> {
            logger.debug("Expected notification count " + expectedNotificationCount);
            logger.debug("Expected code locations:");
            codeLocationNames.forEach(codeLocation -> logger.debug(String.format("  Code Location -> %s", codeLocation)));
            logger.debug("");
        };

        CodeLocationWaitJobChecker codeLocationWaitJobChecker = new CodeLocationWaitJobChecker(logger, blackDuckApiClient, projectService, notificationService, userView, notificationTaskRange, projectAndVersion, codeLocationNames,
            expectedNotificationCount);

        WaitJobCompleter<CodeLocationWaitResult> taskCompleter = () -> {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(codeLocationWaitJobChecker.getFoundCodeLocationNames());
        };

        WaitJobTimeoutHandler<CodeLocationWaitResult> timeoutHandler = () -> {
            String errorMessage = ERROR_MESSAGE.apply(timeoutInSeconds);
            return CodeLocationWaitResult.PARTIAL(codeLocationWaitJobChecker.getFoundCodeLocationNames(), errorMessage);
        };

        WaitJobConfig waitJobConfig = new WaitJobConfig(logger, timeoutInSeconds, notificationTaskRange.getTaskStartTime(), waitIntervalInSeconds);
        WaitJobTask waitJobTask = new WaitJobTask("codeLocationWait", initializer, codeLocationWaitJobChecker, taskCompleter, timeoutHandler);
        WaitJob<CodeLocationWaitResult> waitJob = new WaitJob<>(waitJobConfig, waitJobTask);

        return waitJob.waitFor();
    }

}
