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
package com.synopsys.integration.blackduck.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.blackduck.api.enumeration.ScanSummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.view.ScanSummaryView;
import com.synopsys.integration.blackduck.exception.HubTimeoutExceededException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ScanStatusService extends DataService {
    public static final long FIVE_SECONDS = 5L * 1000;
    public static final long DEFAULT_TIMEOUT = 300000L;
    private static final Set<ScanSummaryStatusType> PENDING_STATES = EnumSet.of(ScanSummaryStatusType.UNSTARTED, ScanSummaryStatusType.SCANNING, ScanSummaryStatusType.SAVING_SCAN_DATA, ScanSummaryStatusType.SCAN_DATA_SAVE_COMPLETE,
            ScanSummaryStatusType.REQUESTED_MATCH_JOB, ScanSummaryStatusType.MATCHING, ScanSummaryStatusType.BOM_VERSION_CHECK, ScanSummaryStatusType.BUILDING_BOM);
    private static final Set<ScanSummaryStatusType> DONE_STATES = EnumSet.of(ScanSummaryStatusType.COMPLETE, ScanSummaryStatusType.CANCELLED, ScanSummaryStatusType.CLONED, ScanSummaryStatusType.ERROR_SCANNING,
            ScanSummaryStatusType.ERROR_SAVING_SCAN_DATA, ScanSummaryStatusType.ERROR_MATCHING, ScanSummaryStatusType.ERROR_BUILDING_BOM, ScanSummaryStatusType.ERROR);
    private static final Set<ScanSummaryStatusType> ERROR_STATES = EnumSet.of(ScanSummaryStatusType.CANCELLED, ScanSummaryStatusType.ERROR_SCANNING, ScanSummaryStatusType.ERROR_SAVING_SCAN_DATA, ScanSummaryStatusType.ERROR_MATCHING,
            ScanSummaryStatusType.ERROR_BUILDING_BOM, ScanSummaryStatusType.ERROR);
    private final ProjectService projectDataService;
    private final CodeLocationService codeLocationDataService;
    private final long timeoutInMilliseconds;

    public ScanStatusService(final HubService hubService, final IntLogger logger, final ProjectService projectDataService, final CodeLocationService codeLocationDataService, final long timeoutInMilliseconds) {
        super(hubService, logger);
        this.projectDataService = projectDataService;
        this.codeLocationDataService = codeLocationDataService;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0L) {
            timeout = DEFAULT_TIMEOUT;
            this.logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    public void assertScansCompleted() {

    }

    /**
     * For the provided projectName and projectVersion, wait at most timeoutInMilliseconds for the project/version to exist and/or at least one pending bom import scan to begin. Then, wait at most timeoutInMilliseconds for all discovered
     * pending scans to complete.
     * <p>
     * If the timeouts are exceeded, a HubTimeoutExceededException will be thrown.
     */
    public void assertBomImportScanStartedThenFinished(final String projectName, final String projectVersion) throws InterruptedException, HubTimeoutExceededException, IntegrationException {
    }

    /**
     * For the given pendingScans, wait at most timeoutInMilliseconds for the scans to complete.
     * <p>
     * If the timeout is exceeded, a HubTimeoutExceededException will be thrown.
     */
    public void assertScansFinished(final List<ScanSummaryView> pendingScans) throws InterruptedException, HubTimeoutExceededException, IntegrationException {
    }

    public void assertScansFinished(final String projectName, final String projectVersion) throws InterruptedException, IntegrationException {
    }

    public void assertScansFinished(final ProjectVersionView projectVersionView) throws InterruptedException, HubTimeoutExceededException, IntegrationException {
    }

    public boolean isPending(final ScanSummaryStatusType statusEnum) {
        return PENDING_STATES.contains(statusEnum);
    }

    public boolean isDone(final ScanSummaryStatusType statusEnum) {
        return DONE_STATES.contains(statusEnum);
    }

    public boolean isError(final ScanSummaryStatusType statusEnum) {
        return ERROR_STATES.contains(statusEnum);
    }

}
