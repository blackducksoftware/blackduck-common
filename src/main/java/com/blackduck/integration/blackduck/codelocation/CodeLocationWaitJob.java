/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.blackduck.service.request.NotificationEditor;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.exception.IntegrationTimeoutException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.NameVersion;
import com.blackduck.integration.wait.ResilientJob;

import java.util.*;
import java.util.stream.Collectors;

public class CodeLocationWaitJob implements ResilientJob<CodeLocationWaitResult> {
    private final IntLogger logger;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    private final UserView userView;
    private final NotificationTaskRange notificationTaskRange;
    private final NameVersion projectAndVersion;
    private final Set<String> codeLocationNames;
    private final int expectedNotificationCount;
    private final CodeLocationsRetriever codeLocationsRetriever;

    private final Set<String> foundCodeLocationNames = new HashSet<>();
    private boolean complete;

    public CodeLocationWaitJob(
        final IntLogger logger,
        final ProjectService projectService,
        final NotificationService notificationService,
        final UserView userView,
        final NotificationTaskRange notificationTaskRange,
        final NameVersion projectAndVersion,
        final Set<String> codeLocationNames,
        final int expectedNotificationCount,
        final BlackDuckApiClient blackDuckApiClient
    ) {
        this.logger = logger;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.userView = userView;
        this.notificationTaskRange = notificationTaskRange;
        this.projectAndVersion = projectAndVersion;
        this.codeLocationNames = codeLocationNames;
        this.expectedNotificationCount = expectedNotificationCount;
        codeLocationsRetriever = new CodeLocationsRetriever(blackDuckApiClient);
    }

    @Override
    public void attemptJob() throws IntegrationException {
        int actualNotificationCount = retrieveCompletedCount(userView, notificationTaskRange);

        complete = foundCodeLocationNames.containsAll(codeLocationNames) && actualNotificationCount >= expectedNotificationCount;
        if (!complete) {
            logger.info("All code locations have not been added to the BOM yet...");
        }
    }

    private int retrieveCompletedCount(UserView userView, NotificationTaskRange notificationTaskRange) throws IntegrationException {
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectAndVersion);
        if (!projectVersionWrapper.isPresent()) {
            return 0;
        }
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();

        Map<String, String> foundCodeLocations = codeLocationsRetriever.retrieveCodeLocations(projectVersionView, codeLocationNames);

        int actualNotificationCount = 0;
        if (foundCodeLocations.size() > 0) {
            logger.debug("At least one code location has been found, now looking for notifications.");
            List<VersionBomCodeLocationBomComputedNotificationUserView> notifications = getFilteredNotificationUserViews(userView, notificationTaskRange);
            logger.debug(String.format("There were %d notifications found.", notifications.size()));

            for (VersionBomCodeLocationBomComputedNotificationUserView notification : notifications) {
                String codeLocationUrl = notification.getContent().getCodeLocation();
                if (foundCodeLocations.containsKey(codeLocationUrl)) {
                    String codeLocationName = foundCodeLocations.get(codeLocationUrl);
                    foundCodeLocationNames.add(codeLocationName);
                    actualNotificationCount++;
                    logger.info(String.format("Found %s code location (%d of %d).", codeLocationName, actualNotificationCount, expectedNotificationCount));
                }
            }
        }

        return actualNotificationCount;
    }

    private List<VersionBomCodeLocationBomComputedNotificationUserView> getFilteredNotificationUserViews(UserView userView, NotificationTaskRange notificationTaskRange) throws IntegrationException {
        Date startDate = notificationTaskRange.getStartDate();
        Date endDate = notificationTaskRange.getEndDate();
        List<String> typesToInclude = Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name());
        NotificationEditor notificationEditor = new NotificationEditor(startDate, endDate, typesToInclude);
        List<NotificationUserView> notifications = notificationService.getAllUserNotifications(userView, notificationEditor);

        return notifications
            .stream()
            .map(VersionBomCodeLocationBomComputedNotificationUserView.class::cast)
            .collect(Collectors.toList());
    }

    @Override
    public boolean wasJobCompleted() {
        return complete;
    }

    @Override
    public CodeLocationWaitResult onTimeout() throws IntegrationTimeoutException {
        String errorMessage = "It was not possible to verify the code locations were added to the BOM within the timeout provided.";
        return CodeLocationWaitResult.PARTIAL(foundCodeLocationNames, errorMessage);
    }

    @Override
    public CodeLocationWaitResult onCompletion() {
        if (complete) {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(foundCodeLocationNames);
        } else {
            return CodeLocationWaitResult.PARTIAL(foundCodeLocationNames, "Not all code locations have been added to the BOM.");
        }
    }

    @Override
    public String getName() {
        return "code location";
    }
}
