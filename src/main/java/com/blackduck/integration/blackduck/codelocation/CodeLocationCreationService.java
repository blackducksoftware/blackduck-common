/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.dataservice.UserService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class CodeLocationCreationService extends DataService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 60;

    private final CodeLocationWaiter codeLocationWaiter;
    private final NotificationService notificationService;
    private final UserService userService;

    public CodeLocationCreationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, CodeLocationWaiter codeLocationWaiter, NotificationService notificationService, UserService userService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.codeLocationWaiter = codeLocationWaiter;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    public <T extends CodeLocationBatchOutput<?>> CodeLocationCreationData<T> createCodeLocations(CodeLocationCreationRequest<T> codeLocationCreationRequest) throws IntegrationException {
        NotificationTaskRange notificationTaskRange = calculateCodeLocationRange();
        T output = codeLocationCreationRequest.executeRequest();

        return new CodeLocationCreationData<>(notificationTaskRange, output);
    }

    public <T extends CodeLocationBatchOutput<?>> T createCodeLocationsAndWait(CodeLocationCreationRequest<T> codeLocationCreationRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return createCodeLocationsAndWait(codeLocationCreationRequest, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public <T extends CodeLocationBatchOutput<?>> T createCodeLocationsAndWait(CodeLocationCreationRequest<T> codeLocationCreationRequest, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        CodeLocationCreationData<T> codeLocationCreationData = createCodeLocations(codeLocationCreationRequest);

        NotificationTaskRange notificationTaskRange = codeLocationCreationData.getNotificationTaskRange();
        T output = codeLocationCreationData.getOutput();

        Optional<NameVersion> projectAndVersion = output.getProjectAndVersion();
        if (projectAndVersion.isPresent()) {
            waitForCodeLocations(notificationTaskRange, projectAndVersion.get(), output.getSuccessfulCodeLocationNames(), output.getExpectedNotificationCount(), timeoutInSeconds, waitIntervalInSeconds);
        } else {
            logger.info("Cannot wait for a code location that is not mapped to a project version. Skipping.");
        }

        return output;
    }

    public CodeLocationWaitResult waitForCodeLocations(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public CodeLocationWaitResult waitForCodeLocations(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        UserView currentUser = userService.findCurrentUser();
        return codeLocationWaiter.checkCodeLocationsAddedToBom(currentUser, notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds, waitIntervalInSeconds);
    }

    public NotificationTaskRange calculateCodeLocationRange() throws IntegrationException {
        long startTime = System.currentTimeMillis();
        LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        LocalDateTime threeDaysLater = localStartTime.plusDays(3);

        UrlSingleResponse<UserView> userResponse = apiDiscovery.metaSingleResponse(ApiDiscovery.CURRENT_USER_PATH);
        UserView currentUser = blackDuckApiClient.getResponse(userResponse);
        Date startDate = notificationService.getLatestUserNotificationDate(currentUser);
        Date endDate = Date.from(threeDaysLater.atZone(ZoneOffset.UTC).toInstant());

        return new NotificationTaskRange(startTime, startDate, endDate);
    }

}
