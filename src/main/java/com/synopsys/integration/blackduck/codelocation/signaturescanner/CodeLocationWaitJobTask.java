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
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJobTask;

import java.util.*;

public class CodeLocationWaitJobTask implements WaitJobTask {
    private final IntLogger logger;
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;

    private final UserView userView;
    private final NotificationTaskRange notificationTaskRange;
    private final Set<String> codeLocationNames;
    private final int expectedNotificationCount;

    private final Set<String> foundCodeLocationNames = new HashSet<>();
    private final Map<String, CodeLocationView> codeLocationNamesToViews = new HashMap<>();
    private final Map<String, String> codeLocationUrlsToNames = new HashMap<>();

    public CodeLocationWaitJobTask(IntLogger logger, CodeLocationService codeLocationService, NotificationService notificationService, UserView userView, NotificationTaskRange notificationTaskRange, Set<String> codeLocationNames, int expectedNotificationCount) {
        this.logger = logger;
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
        this.userView = userView;
        this.notificationTaskRange = notificationTaskRange;
        this.codeLocationNames = codeLocationNames;
        this.expectedNotificationCount = expectedNotificationCount;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        int actualNotificationCount = retrieveCompletedCount(userView, notificationTaskRange, codeLocationNames, expectedNotificationCount);

        boolean complete = foundCodeLocationNames.containsAll(codeLocationNames) && actualNotificationCount >= expectedNotificationCount;
        if (!complete) {
                logger.info("All code locations have not been added to the BOM yet...");
        }

        return complete;
    }

    public Set<String> getFoundCodeLocationNames() {
        return foundCodeLocationNames;
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
