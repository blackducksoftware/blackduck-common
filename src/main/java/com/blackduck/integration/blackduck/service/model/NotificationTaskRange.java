/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import java.util.Date;

public class NotificationTaskRange {
    private final long taskStartTime;
    private final Date startDate;
    private final Date endDate;

    public NotificationTaskRange(final long taskStartTime, final Date startDate, final Date endDate) {
        this.taskStartTime = taskStartTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

}