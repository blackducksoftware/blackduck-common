/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice.scan;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubTimeoutExceededException;
import com.blackducksoftware.integration.hub.model.enumeration.CodeLocationEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ScanSummaryStatusEnum;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.log.IntLogger;

public class ScanStatusDataService {
    public static final long FIVE_SECONDS = 5 * 1000;
    public static final long DEFAULT_TIMEOUT = 300000L;

    private final IntLogger logger;
    private final ProjectService projectRequestService;
    private final ProjectVersionService projectVersionRequestService;
    private final CodeLocationService codeLocationRequestService;
    private final ScanSummaryService scanSummaryRequestService;
    private final MetaHandler metaService;
    private final long timeoutInMilliseconds;

    public ScanStatusDataService(final IntLogger logger, final ProjectService projectRequestService, final ProjectVersionService projectVersionRequestService, final CodeLocationService codeLocationRequestService,
            final ScanSummaryService scanSummaryRequestService, final long timeoutInMilliseconds) {
        this.logger = logger;
        this.metaService = new MetaHandler(logger);
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.codeLocationRequestService = codeLocationRequestService;
        this.scanSummaryRequestService = scanSummaryRequestService;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0L) {
            timeout = DEFAULT_TIMEOUT;
            logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    /**
     * For the provided projectName and projectVersion, wait at most timeoutInMilliseconds for the project/version to exist and/or at least one pending bom import scan to begin. Then, wait at most timeoutInMilliseconds for all discovered
     * pending scans to complete.
     *
     * If the timeouts are exceeded, a HubTimeoutExceededException will be thrown.
     *
     */
    public void assertBomImportScanStartedThenFinished(final String projectName, final String projectVersion) throws HubTimeoutExceededException, IntegrationException {
        final List<ScanSummaryView> pendingScans = waitForPendingScansToStart(projectName, projectVersion, timeoutInMilliseconds);
        waitForScansToComplete(pendingScans, timeoutInMilliseconds);
    }

    public void assertCodeLocationFinished(final String codeLocationName) throws HubTimeoutExceededException, IntegrationException {
        final List<ScanSummaryView> pendingScans = new ArrayList<>();

        boolean foundPendingScan = false;
        final long startedTime = System.currentTimeMillis();
        final String timeoutMessage = "No pending code locations found within the specified wait time: %d minutes";
        while (!done(foundPendingScan, timeoutInMilliseconds, startedTime, timeoutMessage)) {
            try {
                final CodeLocationView codeLocation = codeLocationRequestService.getCodeLocationByName(codeLocationName);
                final String scanSummariesLink = metaService.getFirstLinkSafely(codeLocation, MetaHandler.SCANS_LINK);
                if (StringUtils.isNotBlank(scanSummariesLink)) {
                    final ScanSummaryView scanSummaryView = scanSummaryRequestService.getView(scanSummariesLink, ScanSummaryView.class);
                    if (isPending(scanSummaryView.status)) {
                        pendingScans.add(scanSummaryView);
                    }
                }
                foundPendingScan = pendingScans.size() > 0;
            } catch (final IntegrationException e) {
                // ignore, since we might not have found a project or version, etc
                // so just keep waiting until the timeout
                logger.debug("Could not find a pending code location: " + e.getMessage());
            }
        }
    }

    /**
     * For the given pendingScans, wait at most timeoutInMilliseconds for the scans to complete.
     *
     * If the timeout is exceeded, a HubTimeoutExceededException will be thrown.
     *
     */
    public void assertScansFinished(final List<ScanSummaryView> pendingScans) throws HubTimeoutExceededException, IntegrationException {
        waitForScansToComplete(pendingScans, timeoutInMilliseconds);
    }

    public void assertScansFinished(final String projectName, final String projectVersion) throws IntegrationException {
        final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView projectVersionView = projectVersionRequestService.getProjectVersion(projectItem, projectVersion);
        assertScansFinished(projectVersionView);
    }

    public void assertScansFinished(final ProjectVersionView projectVersionView) throws HubTimeoutExceededException, IntegrationException {
        final List<CodeLocationView> allCodeLocations = codeLocationRequestService.getAllCodeLocationsForProjectVersion(projectVersionView);
        final List<ScanSummaryView> scanSummaryViews = new ArrayList<>();
        for (final CodeLocationView codeLocationView : allCodeLocations) {
            final String scansLink = metaService.getFirstLinkSafely(codeLocationView, MetaHandler.SCANS_LINK);
            final List<ScanSummaryView> codeLocationScanSummaryViews = scanSummaryRequestService.getAllScanSummaryItems(scansLink);
            scanSummaryViews.addAll(codeLocationScanSummaryViews);
        }
        assertScansFinished(scanSummaryViews);
    }

    private List<ScanSummaryView> waitForPendingScansToStart(final String projectName, final String projectVersion, final long scanStartedTimeoutInMilliseconds) throws HubIntegrationException {
        List<ScanSummaryView> pendingScans = getPendingScans(projectName, projectVersion);
        final long startedTime = System.currentTimeMillis();
        boolean pendingScansOk = pendingScans.size() > 0;
        final String timeoutMessage = "No scan has started within the specified wait time: %d minutes";
        while (!done(pendingScansOk, scanStartedTimeoutInMilliseconds, startedTime, timeoutMessage)) {
            sleep("The thread waiting for the scan to start was interrupted: ", "Still waiting for the pending scans to start.");
            pendingScans = getPendingScans(projectName, projectVersion);
            pendingScansOk = pendingScans.size() > 0;
        }

        return pendingScans;
    }

    private void waitForScansToComplete(List<ScanSummaryView> pendingScans, final long scanStartedTimeoutInMilliseconds) throws HubTimeoutExceededException, IntegrationException {
        pendingScans = getPendingScans(pendingScans);
        final long startedTime = System.currentTimeMillis();
        boolean pendingScansOk = pendingScans.isEmpty();
        final String timeoutMessage = "The pending scans have not completed within the specified wait time: %d minutes";
        while (!done(pendingScansOk, scanStartedTimeoutInMilliseconds, startedTime, timeoutMessage)) {
            sleep("The thread waiting for the scan to complete was interrupted: ", "Still waiting for the pending scans to complete.");
            pendingScans = getPendingScans(pendingScans);
            pendingScansOk = pendingScans.isEmpty();
        }
    }

    private void sleep(final String interruptedMessage, final String ongoingMessage) throws HubIntegrationException {
        try {
            logger.info(ongoingMessage);
            Thread.sleep(FIVE_SECONDS);
        } catch (final InterruptedException e) {
            throw new HubIntegrationException(interruptedMessage + e.getMessage(), e);
        }
    }

    private boolean done(final boolean conditionToCheck, final long timeoutInMilliseconds, final long startedTime, final String timeoutMessage) throws HubTimeoutExceededException {
        if (conditionToCheck) {
            return true;
        }

        if (takenTooLong(timeoutInMilliseconds, startedTime)) {
            throw new HubTimeoutExceededException(String.format(timeoutMessage, TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds)));
        }

        return false;
    }

    private boolean takenTooLong(final long timeoutInMilliseconds, final long startedTime) {
        final long elapsed = System.currentTimeMillis() - startedTime;
        return elapsed > timeoutInMilliseconds;
    }

    private List<ScanSummaryView> getPendingScans(final String projectName, final String projectVersion) {
        List<ScanSummaryView> pendingScans = new ArrayList<>();
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
            final ProjectVersionView projectVersionItem = projectVersionRequestService.getProjectVersion(projectItem, projectVersion);
            final String projectVersionUrl = metaService.getHref(projectVersionItem);

            final List<CodeLocationView> allCodeLocations = codeLocationRequestService.getAllCodeLocationsForCodeLocationType(CodeLocationEnum.BOM_IMPORT);

            final List<String> allScanSummariesLinks = new ArrayList<>();
            for (final CodeLocationView codeLocationItem : allCodeLocations) {
                logger.debug("Checking codeLocation: " + codeLocationItem.name);
                final String mappedProjectVersionUrl = codeLocationItem.mappedProjectVersion;
                if (projectVersionUrl.equals(mappedProjectVersionUrl)) {
                    final String scanSummariesLink = metaService.getFirstLink(codeLocationItem, MetaHandler.SCANS_LINK);
                    allScanSummariesLinks.add(scanSummariesLink);
                }
            }

            final List<ScanSummaryView> allScanSummaries = new ArrayList<>();
            for (final String scanSummaryLink : allScanSummariesLinks) {
                allScanSummaries.addAll(scanSummaryRequestService.getAllScanSummaryItems(scanSummaryLink));
            }

            pendingScans = new ArrayList<>();
            for (final ScanSummaryView scanSummaryItem : allScanSummaries) {
                if (isPending(scanSummaryItem.status)) {
                    logger.debug("Adding pending scan: " + scanSummaryItem.json);
                    pendingScans.add(scanSummaryItem);
                }
            }
        } catch (final Exception e) {
            // ignore, since we might not have found a project or version, etc
            // so just keep waiting until the timeout
            pendingScans = new ArrayList<>();
            logger.debug("Not able to get pending scans: " + e.getMessage());
        }

        return pendingScans;
    }

    private List<ScanSummaryView> getPendingScans(final List<ScanSummaryView> scanSummaries) throws IntegrationException {
        final List<ScanSummaryView> pendingScans = new ArrayList<>();
        for (final ScanSummaryView scanSummaryItem : scanSummaries) {
            final String scanSummaryLink = metaService.getHref(scanSummaryItem);
            final ScanSummaryView currentScanSummaryItem = scanSummaryRequestService.getView(scanSummaryLink, ScanSummaryView.class);
            if (isPending(currentScanSummaryItem.status)) {
                pendingScans.add(currentScanSummaryItem);
            } else if (isError(currentScanSummaryItem.status)) {
                throw new HubIntegrationException("There was a problem in the Hub processing the scan(s). Error Status : " + currentScanSummaryItem.status.toString() + ", " + currentScanSummaryItem.statusMessage);
            }
        }

        return pendingScans;
    }

    private static final Set<ScanSummaryStatusEnum> PENDING_STATES = EnumSet.of(ScanSummaryStatusEnum.UNSTARTED, ScanSummaryStatusEnum.SCANNING, ScanSummaryStatusEnum.SAVING_SCAN_DATA, ScanSummaryStatusEnum.SCAN_DATA_SAVE_COMPLETE,
            ScanSummaryStatusEnum.REQUESTED_MATCH_JOB, ScanSummaryStatusEnum.MATCHING, ScanSummaryStatusEnum.BOM_VERSION_CHECK, ScanSummaryStatusEnum.BUILDING_BOM);

    private static final Set<ScanSummaryStatusEnum> DONE_STATES = EnumSet.of(ScanSummaryStatusEnum.COMPLETE, ScanSummaryStatusEnum.CANCELLED, ScanSummaryStatusEnum.CLONED, ScanSummaryStatusEnum.ERROR_SCANNING,
            ScanSummaryStatusEnum.ERROR_SAVING_SCAN_DATA, ScanSummaryStatusEnum.ERROR_MATCHING, ScanSummaryStatusEnum.ERROR_BUILDING_BOM, ScanSummaryStatusEnum.ERROR);

    private static final Set<ScanSummaryStatusEnum> ERROR_STATES = EnumSet.of(ScanSummaryStatusEnum.CANCELLED, ScanSummaryStatusEnum.ERROR_SCANNING, ScanSummaryStatusEnum.ERROR_SAVING_SCAN_DATA, ScanSummaryStatusEnum.ERROR_MATCHING,
            ScanSummaryStatusEnum.ERROR_BUILDING_BOM, ScanSummaryStatusEnum.ERROR);

    public boolean isPending(final ScanSummaryStatusEnum statusEnum) {
        return PENDING_STATES.contains(statusEnum);
    }

    public boolean isDone(final ScanSummaryStatusEnum statusEnum) {
        return DONE_STATES.contains(statusEnum);
    }

    public boolean isError(final ScanSummaryStatusEnum statusEnum) {
        return ERROR_STATES.contains(statusEnum);
    }

}
