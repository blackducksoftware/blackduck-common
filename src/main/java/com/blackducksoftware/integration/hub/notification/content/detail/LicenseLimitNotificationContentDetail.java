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
package com.blackducksoftware.integration.hub.notification.content.detail;

import java.util.Collections;
import java.util.List;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.LicenseLimitType;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;

public class LicenseLimitNotificationContentDetail extends NotificationContentDetail2 {
    private final LicenseLimitType licenseViolationType;
    private final String message;
    private final String marketingPageUrl;
    private final Long usedCodeSize;
    private final Long hardLimit;
    private final Long softLimit;

    // @formatter:off
    public LicenseLimitNotificationContentDetail(
             final LicenseLimitType licenseViolationType
            ,final String message
            ,final String marketingPageUrl
            ,final Long usedCodeSize
            ,final Long hardLimit
            ,final Long softLimit
            ) {
        super(NotificationTypeGrouping.LICENSE);
        this.licenseViolationType = licenseViolationType;
        this.message = message;
        this.marketingPageUrl = marketingPageUrl;
        this.usedCodeSize = usedCodeSize;
        this.hardLimit = hardLimit;
        this.softLimit = softLimit;
    }
    // @formatter:on

    @Override
    public List<UriSingleResponse<? extends HubResponse>> getPresentLinks() {
        // TODO Unknown currently
        return Collections.emptyList();
    }

    public LicenseLimitType getLicenseViolationType() {
        return licenseViolationType;
    }

    public String getMessage() {
        return message;
    }

    public String getMarketingPageUrl() {
        return marketingPageUrl;
    }

    public Long getUsedCodeSize() {
        return usedCodeSize;
    }

    public Long getHardLimit() {
        return hardLimit;
    }

    public Long getSoftLimit() {
        return softLimit;
    }

}
