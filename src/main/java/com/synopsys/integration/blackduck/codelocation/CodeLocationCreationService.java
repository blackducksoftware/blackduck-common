/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.exception.HubTimeoutExceededException;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class CodeLocationCreationService extends DataService {
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;

    public CodeLocationCreationService(final HubService hubService, final IntLogger logger, final CodeLocationService codeLocationService, final NotificationService notificationService) {
        super(hubService, logger);
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
    }

    public <T extends CodeLocationBatchOutput> CodeLocationCreationData<T> createCodeLocations(final CodeLocationCreationRequest<T> codeLocationCreationRequest) throws IntegrationException {
        final NotificationTaskRange notificationTaskRange = calculateCodeLocationRange();
        final T output = codeLocationCreationRequest.executeRequest();

        final Set<String> successfulCodeLocationNames = output.getSuccessfulCodeLocationNames();
        return new CodeLocationCreationData<>(notificationTaskRange, successfulCodeLocationNames, output);
    }

    public <T extends CodeLocationBatchOutput> T createCodeLocationsAndWait(final CodeLocationCreationRequest<T> codeLocationCreationRequest, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        final CodeLocationCreationData<T> codeLocationCreationData = createCodeLocations(codeLocationCreationRequest);

        final NotificationTaskRange notificationTaskRange = codeLocationCreationData.getNotificationTaskRange();
        final Set<String> successfulCodeLocationNames = codeLocationCreationData.getSuccessfulCodeLocationNames();
        final T output = codeLocationCreationData.getOutput();

        waitForCodeLocations(notificationTaskRange, successfulCodeLocationNames, timeoutInSeconds);

        return output;
    }

    public void waitForCodeLocations(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        assertCodeLocationsAddedToBom(notificationTaskRange, codeLocationNames, timeoutInSeconds);
    }

    public NotificationTaskRange calculateCodeLocationRange() throws IntegrationException {
        final long startTime = System.currentTimeMillis();
        final LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        final LocalDateTime threeDaysLater = localStartTime.plusDays(3);

        final Date startDate = notificationService.getLatestNotificationDate();
        final Date endDate = Date.from(threeDaysLater.atZone(ZoneOffset.UTC).toInstant());

        return new NotificationTaskRange(startTime, startDate, endDate);
    }

    private void assertCodeLocationsAddedToBom(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        boolean allCompleted = false;
        int attemptCount = 1;
        while (!allCompleted && System.currentTimeMillis() - notificationTaskRange.getTaskStartTime() <= timeoutInSeconds * 1000) {
            final List<CodeLocationView> codeLocations = new ArrayList<>();
            for (final String codeLocationName : codeLocationNames) {
                try {
                    final CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
                    codeLocations.add(codeLocationView);
                } catch (final DoesNotExistException ignoreBecauseItIsExpected) {
                    // ignored - the code locations may not exist yet, hence the searching and timeout
                }
            }

            if (codeLocations.size() == codeLocationNames.size()) {
                logger.debug("All code locations have been found, now looking for notifications.");
                final Set<String> codeLocationUrlsToFind = codeLocations
                                                                   .stream()
                                                                   .map(CodeLocationView::getHref)
                                                                   .filter(Optional::isPresent)
                                                                   .map(Optional::get)
                                                                   .collect(Collectors.toSet());
                final Set<String> codeLocationUrls = new HashSet<>();
                final List<NotificationView> notifications = notificationService
                                                                     .getFilteredNotifications(notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(),
                                                                             Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
                logger.debug(String.format("There were %d notifications found.", notifications.size()));

                for (final NotificationView notificationView : notifications) {
                    final Optional<String> codeLocationUrl = getCodeLocationUrl(notificationView);
                    if (codeLocationUrl.isPresent()) {
                        codeLocationUrls.add(codeLocationUrl.get());
                    }
                }

                if (codeLocationUrls.containsAll(codeLocationUrlsToFind)) {
                    allCompleted = true;
                } else {
                    attemptCount++;
                    logger.info(String.format("All code locations have not been added to the BOM yet, waiting another 5 seconds (try #%d)...", attemptCount));
                    Thread.sleep(5000);
                }
            }
        }

        if (!allCompleted) {
            throw new HubTimeoutExceededException(String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds));
        } else {
            logger.info("All code locations have been added to the BOM.");
        }
    }

    private Optional<String> getCodeLocationUrl(final NotificationView notificationView) {
        final String codeLocationUrl = JsonPath.read(notificationView.getJson(), "$.content.codeLocation");
        return Optional.ofNullable(codeLocationUrl);
    }

}
