/**
 * Hub Common
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
package com.blackducksoftware.integration.hub.scan;

import com.blackducksoftware.integration.validator.FieldEnum;

public enum HubScanConfigFieldEnum implements FieldEnum {
    PROJECT("hubProject"),
    VERSION("hubVersion"),
    PHASE("hubPhase"),
    DISTRIBUTION("hubDistribution"),
    PROJECT_LEVEL_ADJUSTMENTS("projectLevelAdjustments"),
    GENERATE_RISK_REPORT("shouldGenerateRiskReport"),
    DRY_RUN("dryRun"),
    CLEANUP_LOGS_ON_SUCCESS("cleanupLogsOnSuccess"),
    UNMAP_PREVIOUS_CODE_LOCATIONS("unmapPreviousCodeLocations"),
    DELETE_PREVIOUS_CODE_LOCATIONS("deletePreviousCodeLocations"),
    EXCLUDE_PATTERNS("excludePatterns"),
    CODE_LOCATION_ALIAS("codeLocationAlias"),
    MAX_WAIT_TIME_FOR_BOM_UPDATE("maxWaitTimeForBomUpdate"),
    SCANMEMORY("hubScanMemory"),
    TARGETS("hubTargets"),
    FAIL_ON_POLICY_VIOLATION("failOnPolicyViolation");

    private String key;

    private HubScanConfigFieldEnum(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

}
