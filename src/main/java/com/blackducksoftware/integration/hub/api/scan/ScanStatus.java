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
package com.blackducksoftware.integration.hub.api.scan;

import java.util.EnumSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public enum ScanStatus {
    UNSTARTED,
    SCANNING,
    SAVING_SCAN_DATA,
    SCAN_DATA_SAVE_COMPLETE,
    REQUESTED_MATCH_JOB,
    MATCHING,
    BOM_VERSION_CHECK,
    BUILDING_BOM,
    CLONED,
    COMPLETE,
    CANCELLED,
    ERROR_SCANNING,
    ERROR_SAVING_SCAN_DATA,
    ERROR_MATCHING,
    ERROR_BUILDING_BOM,
    ERROR;

    private static final Set<ScanStatus> PENDING_STATES = EnumSet.of(UNSTARTED, SCANNING, SAVING_SCAN_DATA,
            SCAN_DATA_SAVE_COMPLETE, REQUESTED_MATCH_JOB, MATCHING, BOM_VERSION_CHECK, BUILDING_BOM);

    private static final Set<ScanStatus> DONE_STATES = EnumSet.of(COMPLETE, CANCELLED, CLONED, ERROR_SCANNING,
            ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR);

    private static final Set<ScanStatus> ERROR_STATES = EnumSet.of(CANCELLED, ERROR_SCANNING, ERROR_SAVING_SCAN_DATA,
            ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR);

    public boolean isPending() {
        return PENDING_STATES.contains(this);
    }

    public boolean isDone() {
        return DONE_STATES.contains(this);
    }

    public boolean isError() {
        return ERROR_STATES.contains(this);
    }

    public static ScanStatus getScanStatus(final String scanStatus) throws HubIntegrationException {
        if (scanStatus == null) {
            return null;
        }
        ScanStatus scanStatusEnum;
        try {
            scanStatusEnum = ScanStatus.valueOf(scanStatus.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new HubIntegrationException("Unknown Scan Status : " + scanStatus);
        }
        return scanStatusEnum;
    }

}
