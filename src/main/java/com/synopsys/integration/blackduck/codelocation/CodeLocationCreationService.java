/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.Date;
import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class CodeLocationCreationService extends DataService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 60;

    private final CodeLocationWaiter codeLocationWaiter;
    private final NotificationService notificationService;

    public CodeLocationCreationService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, CodeLocationWaiter codeLocationWaiter, NotificationService notificationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.codeLocationWaiter = codeLocationWaiter;
        this.notificationService = notificationService;
    }

    public <T extends CodeLocationBatchOutput> CodeLocationCreationData<T> createCodeLocations(CodeLocationCreationRequest<T> codeLocationCreationRequest) throws IntegrationException {
        NotificationTaskRange notificationTaskRange = calculateCodeLocationRange();
        T output = codeLocationCreationRequest.executeRequest();

        return new CodeLocationCreationData<>(notificationTaskRange, output);
    }

    public <T extends CodeLocationBatchOutput> T createCodeLocationsAndWait(CodeLocationCreationRequest<T> codeLocationCreationRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return createCodeLocationsAndWait(codeLocationCreationRequest, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public <T extends CodeLocationBatchOutput> T createCodeLocationsAndWait(CodeLocationCreationRequest<T> codeLocationCreationRequest, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        CodeLocationCreationData<T> codeLocationCreationData = createCodeLocations(codeLocationCreationRequest);

        NotificationTaskRange notificationTaskRange = codeLocationCreationData.getNotificationTaskRange();
        T output = codeLocationCreationData.getOutput();

        waitForCodeLocations(notificationTaskRange, output.getProjectAndVersion(), output.getSuccessfulCodeLocationNames(), output.getExpectedNotificationCount(), timeoutInSeconds, waitIntervalInSeconds);

        return output;
    }

    public CodeLocationWaitResult waitForCodeLocations(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public CodeLocationWaitResult waitForCodeLocations(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        UserView currentUser = blackDuckApiClient.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        return codeLocationWaiter.checkCodeLocationsAddedToBom(currentUser, notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds, waitIntervalInSeconds);
    }

    public NotificationTaskRange calculateCodeLocationRange() throws IntegrationException {
        long startTime = System.currentTimeMillis();
        LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        LocalDateTime threeDaysLater = localStartTime.plusDays(3);

        UserView currentUser = blackDuckApiClient.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        Date startDate = notificationService.getLatestUserNotificationDate(currentUser);
        Date endDate = Date.from(threeDaysLater.atZone(ZoneOffset.UTC).toInstant());

        return new NotificationTaskRange(startTime, startDate, endDate);
    }

}
