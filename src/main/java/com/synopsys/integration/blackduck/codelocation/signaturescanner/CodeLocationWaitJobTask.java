/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.WaitJobTask;

public class CodeLocationWaitJobTask implements WaitJobTask {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    private final UserView userView;
    private final NotificationTaskRange notificationTaskRange;
    private final NameVersion projectAndVersion;
    private final Set<String> codeLocationNames;
    private final int expectedNotificationCount;

    private final Set<String> foundCodeLocationNames = new HashSet<>();

    public CodeLocationWaitJobTask(IntLogger logger, BlackDuckService blackDuckService, ProjectService projectService, NotificationService notificationService, UserView userView, NotificationTaskRange notificationTaskRange,
        NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        this.projectService = projectService;
        this.notificationService = notificationService;
        this.userView = userView;
        this.notificationTaskRange = notificationTaskRange;
        this.projectAndVersion = projectAndVersion;
        this.codeLocationNames = codeLocationNames;
        this.expectedNotificationCount = expectedNotificationCount;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        int actualNotificationCount = retrieveCompletedCount(userView, notificationTaskRange);

        boolean complete = foundCodeLocationNames.containsAll(codeLocationNames) && actualNotificationCount >= expectedNotificationCount;
        if (!complete) {
            logger.info("All code locations have not been added to the BOM yet...");
        }

        return complete;
    }

    public Set<String> getFoundCodeLocationNames() {
        return foundCodeLocationNames;
    }

    private int retrieveCompletedCount(UserView userView, NotificationTaskRange notificationTaskRange) throws IntegrationException {
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectAndVersion);
        if (!projectVersionWrapper.isPresent()) {
            return 0;
        }
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();

        Map<String, String> foundCodeLocations = retrieveCodeLocations(projectVersionView);

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

    private Map<String, String> retrieveCodeLocations(ProjectVersionView projectVersionView) throws IntegrationException {
        List<CodeLocationView> codeLocationViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        return codeLocationViews
                   .stream()
                   .filter(codeLocationView -> codeLocationNames.contains(codeLocationView.getName()))
                   .collect(Collectors.toMap(codeLocationView -> codeLocationView.getHref().string(), CodeLocationView::getName));
    }

    private List<VersionBomCodeLocationBomComputedNotificationUserView> getFilteredNotificationUserViews(UserView userView, NotificationTaskRange notificationTaskRange) throws IntegrationException {
        List<NotificationUserView> notifications = notificationService
                                                       .getFilteredUserNotifications(userView, notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(),
                                                           Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));

        List<VersionBomCodeLocationBomComputedNotificationUserView> filteredNotifications = notifications
                                                                                                .stream()
                                                                                                .map(notificationView -> (VersionBomCodeLocationBomComputedNotificationUserView) notificationView)
                                                                                                .collect(Collectors.toList());

        return filteredNotifications;
    }

}
