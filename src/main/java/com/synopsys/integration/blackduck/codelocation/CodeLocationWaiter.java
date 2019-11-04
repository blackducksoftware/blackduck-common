/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class CodeLocationWaiter {
    private final IntLogger logger;
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;

    private final Set<String> foundCodeLocationNames = new HashSet<>();
    private final Map<String, CodeLocationView> codeLocationNamesToViews = new HashMap<>();
    private final Map<String, String> codeLocationUrlsToNames = new HashMap<>();

    public CodeLocationWaiter(IntLogger logger, CodeLocationService codeLocationService, NotificationService notificationService) {
        this.logger = logger;
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
    }

    public CodeLocationWaitResult checkCodeLocationsAddedToBom(UserView userView, NotificationTaskRange notificationTaskRange, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
            throws IntegrationException, InterruptedException {
        boolean allCompleted = false;
        int attemptCount = 1;
        do {
            // if a timeout of 0 is provided and the timeout check is done too quickly, w/o a do/while, no check will be performed
            // regardless of the timeout provided, we always want to check at least once
            int actualNotificationCount = retrieveCompletedCount(userView, notificationTaskRange, codeLocationNames, expectedNotificationCount);

            if (foundCodeLocationNames.containsAll(codeLocationNames) && actualNotificationCount >= expectedNotificationCount) {
                allCompleted = true;
            } else {
                attemptCount++;
                logger.info(String.format("All code locations have not been added to the BOM yet, waiting another 5 seconds (try #%d)...", attemptCount));
                Thread.sleep(5000);
            }
        } while (!allCompleted && System.currentTimeMillis() - notificationTaskRange.getTaskStartTime() <= timeoutInSeconds * 1000);

        if (!allCompleted) {
            return CodeLocationWaitResult.PARTIAL(foundCodeLocationNames, String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds));
        } else {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(foundCodeLocationNames);
        }
    }

    private int retrieveCompletedCount(UserView userView, NotificationTaskRange notificationTaskRange, Set<String> codeLocationNames, int expectedNotificationCount) throws IntegrationException {
        for (String codeLocationName : codeLocationNames) {
            if (!codeLocationNamesToViews.containsKey(codeLocationName)) {
                Optional<CodeLocationView> codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
                if (codeLocationView.flatMap(CodeLocationView::getHref).isPresent()) {
                    codeLocationNamesToViews.put(codeLocationName, codeLocationView.get());
                    codeLocationUrlsToNames.put(codeLocationView.get().getHref().get(), codeLocationName);
                }
            }
        }

        int actualNotificationCount = 0;
        if (codeLocationNamesToViews.size() > 0) {
            logger.debug("At least one code location has been found, now looking for notifications.");
            List<NotificationUserView> notifications = notificationService
                    .getFilteredUserNotifications(userView, notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(),
                            Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
            logger.debug(String.format("There were %d notifications found.", notifications.size()));

            for (NotificationUserView notificationView : notifications) {
                Optional<String> codeLocationUrl = getCodeLocationUrl(notificationView);
                if (codeLocationUrl.isPresent() && codeLocationUrlsToNames.containsKey(codeLocationUrl.get())) {
                    String codeLocationName = codeLocationUrlsToNames.get(codeLocationUrl.get());
                    foundCodeLocationNames.add(codeLocationName);
                    actualNotificationCount++;
                    logger.info(String.format("Found %s code location (%d of %d).", codeLocationName, actualNotificationCount, expectedNotificationCount));
                }
            }
        }
        return actualNotificationCount;
    }

    private Optional<String> getCodeLocationUrl(NotificationUserView notificationView) {
        String codeLocationUrl = JsonPath.read(notificationView.getJson(), "$.content.codeLocation");
        return Optional.ofNullable(codeLocationUrl);
    }

}
