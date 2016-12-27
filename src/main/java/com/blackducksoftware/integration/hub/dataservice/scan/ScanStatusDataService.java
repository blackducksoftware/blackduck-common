/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationItem;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationTypeEnum;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubTimeoutExceededException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class ScanStatusDataService extends HubRequestService {
    private static final long FIVE_SECONDS = 5 * 1000;

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final CodeLocationRequestService codeLocationRequestService;

    private final ScanSummaryRequestService scanSummaryRequestService;

    private final MetaService metaService;

    public ScanStatusDataService(final RestConnection restConnection,
            final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService,
            final CodeLocationRequestService codeLocationRequestService,
            final ScanSummaryRequestService scanSummaryRequestService, MetaService metaService) {
        super(restConnection);
        this.metaService = metaService;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.codeLocationRequestService = codeLocationRequestService;
        this.scanSummaryRequestService = scanSummaryRequestService;
    }

    /**
     * For the provided projectName and projectVersion, wait at most
     * scanStartedTimeoutInMilliseconds for the project/version to exist and/or
     * at least one pending bom import scan to begin. Then, wait at most
     * scanFinishedTimeoutInMilliseconds for all discovered pending scans to
     * complete.
     *
     * If the timeouts are exceeded, a HubTimeoutExceededException will be
     * thrown.
     *
     * @param projectRequestService
     * @param projectVersionRequestService
     * @param codeLocationRequestService
     * @param scanSummaryRequestService
     * @param projectName
     * @param projectVersion
     * @param scanStartedTimeoutInMilliseconds
     * @param scanFinishedTimeoutInMilliseconds
     * @param logger
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws ProjectDoesNotExistException
     * @throws UnexpectedHubResponseException
     * @throws HubIntegrationException
     * @throws HubTimeoutExceededException
     * @throws InterruptedException
     */
    public void assertBomImportScanStartedThenFinished(final String projectName, final String projectVersion,
            final long scanStartedTimeoutInMilliseconds, final long scanFinishedTimeoutInMilliseconds,
            final IntLogger logger) throws HubTimeoutExceededException, HubIntegrationException {
        final List<ScanSummaryItem> pendingScans = waitForPendingScansToStart(projectName, projectVersion,
                scanStartedTimeoutInMilliseconds);
        waitForScansToComplete(pendingScans, scanFinishedTimeoutInMilliseconds);
    }

    /**
     * For the given pendingScans, wait at most
     * scanFinishedTimeoutInMilliseconds for the scans to complete.
     *
     * If the timeout is exceeded, a HubTimeoutExceededException will be thrown.
     *
     * @param scanSummaryRequestService
     * @param pendingScans
     * @param scanFinishedTimeoutInMilliseconds
     * @throws InterruptedException
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws HubIntegrationException
     * @throws ProjectDoesNotExistException
     * @throws UnexpectedHubResponseException
     * @throws HubTimeoutExceededException
     */
    public void assertBomImportScansFinished(final List<ScanSummaryItem> pendingScans,
            final long scanFinishedTimeoutInMilliseconds) throws HubTimeoutExceededException, HubIntegrationException {
        waitForScansToComplete(pendingScans, scanFinishedTimeoutInMilliseconds);
    }

    private List<ScanSummaryItem> waitForPendingScansToStart(final String projectName, final String projectVersion,
            final long scanStartedTimeoutInMilliseconds) throws HubIntegrationException {
        List<ScanSummaryItem> pendingScans = getPendingScans(projectName, projectVersion);
        final long startedTime = System.currentTimeMillis();
        boolean pendingScansOk = pendingScans.size() > 0;
        while (!done(pendingScansOk, scanStartedTimeoutInMilliseconds, startedTime,
                "No scan has started within the specified wait time: %d minutes")) {
            try {
                Thread.sleep(FIVE_SECONDS);
            } catch (final InterruptedException e) {
                throw new HubIntegrationException("The thread waiting for the scan to start was interrupted: " + e.getMessage(), e);
            }
            pendingScans = getPendingScans(projectName, projectVersion);
            pendingScansOk = pendingScans.size() > 0;
        }

        return pendingScans;
    }

    private void waitForScansToComplete(List<ScanSummaryItem> pendingScans, final long scanStartedTimeoutInMilliseconds)
            throws HubTimeoutExceededException, HubIntegrationException {
        pendingScans = getPendingScans(pendingScans);
        final long startedTime = System.currentTimeMillis();
        boolean pendingScansOk = pendingScans.isEmpty();
        while (!done(pendingScansOk, scanStartedTimeoutInMilliseconds, startedTime,
                "The pending scans have not completed within the specified wait time: %d minutes")) {
            try {
                Thread.sleep(FIVE_SECONDS);
            } catch (final InterruptedException e) {
                throw new HubIntegrationException("The thread waiting for the scan to complete was interrupted: " + e.getMessage(), e);
            }
            pendingScans = getPendingScans(pendingScans);
            pendingScansOk = pendingScans.isEmpty();
        }
    }

    private boolean done(final boolean pendingScansOk, final long timeoutInMilliseconds, final long startedTime,
            final String timeoutMessage) throws HubTimeoutExceededException {
        if (pendingScansOk) {
            return true;
        }

        if (takenTooLong(timeoutInMilliseconds, startedTime)) {
            throw new HubTimeoutExceededException(
                    String.format(timeoutMessage, TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds)));
        }

        return false;
    }

    private boolean takenTooLong(final long timeoutInMilliseconds, final long startedTime) {
        final long elapsed = System.currentTimeMillis() - startedTime;
        return elapsed > timeoutInMilliseconds;
    }

    private List<ScanSummaryItem> getPendingScans(final String projectName, final String projectVersion) {
        List<ScanSummaryItem> pendingScans = new ArrayList<>();
        try {
            final ProjectItem projectItem = projectRequestService.getProjectByName(projectName);
            final ProjectVersionItem projectVersionItem = projectVersionRequestService.getProjectVersion(projectItem, projectVersion);
            final String projectVersionUrl = metaService.getHref(projectVersionItem);

            final List<CodeLocationItem> allCodeLocations = codeLocationRequestService
                    .getAllCodeLocationsForCodeLocationType(CodeLocationTypeEnum.BOM_IMPORT);

            final List<String> allScanSummariesLinks = new ArrayList<>();
            for (final CodeLocationItem codeLocationItem : allCodeLocations) {
                final String mappedProjectVersionUrl = codeLocationItem.getMappedProjectVersion();
                if (projectVersionUrl.equals(mappedProjectVersionUrl)) {
                    final String scanSummariesLink = metaService.getLink(codeLocationItem, MetaService.SCANS_LINK);
                    allScanSummariesLinks.add(scanSummariesLink);
                }
            }

            final List<ScanSummaryItem> allScanSummaries = new ArrayList<>();
            for (final String scanSummaryLink : allScanSummariesLinks) {
                allScanSummaries.addAll(scanSummaryRequestService.getAllScanSummaryItems(scanSummaryLink));
            }

            pendingScans = new ArrayList<>();
            for (final ScanSummaryItem scanSummaryItem : allScanSummaries) {
                if (scanSummaryItem.getStatus().isPending()) {
                    pendingScans.add(scanSummaryItem);
                }
            }
        } catch (final Exception e) {
            pendingScans = new ArrayList<>();
            // ignore, since we might not have found a project or version, etc
            // so just keep waiting until the timeout
        }

        return pendingScans;
    }

    private List<ScanSummaryItem> getPendingScans(final List<ScanSummaryItem> scanSummaries) throws HubIntegrationException {
        final List<ScanSummaryItem> pendingScans = new ArrayList<>();
        for (final ScanSummaryItem scanSummaryItem : scanSummaries) {
            final String scanSummaryLink = metaService.getHref(scanSummaryItem);
            final ScanSummaryItem currentScanSummaryItem = scanSummaryRequestService.getItem(scanSummaryLink);
            if (currentScanSummaryItem.getStatus().isPending()) {
                pendingScans.add(currentScanSummaryItem);
            } else if (currentScanSummaryItem.getStatus().isError()) {
                throw new HubIntegrationException("There was a problem with one of the scans. Error Status : "
                        + currentScanSummaryItem.getStatus().toString());
            }
        }

        return pendingScans;
    }

}
