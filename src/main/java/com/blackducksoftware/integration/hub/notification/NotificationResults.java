/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.notification;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.util.Stringable;

public abstract class NotificationResults {
    private final Optional<Date> latestNotificationCreatedAtDate;
    private final Optional<String> latestNotificationCreatedAtString;

    public NotificationResults(final Optional<Date> latestNotificationCreatedAtDate, final Optional<String> latestNotificationCreatedAtString) {
        this.latestNotificationCreatedAtDate = latestNotificationCreatedAtDate;
        this.latestNotificationCreatedAtString = latestNotificationCreatedAtString;
    }

    // TODO we can do better than this
    public abstract <T extends Stringable> List<T> getResults();

    public final Optional<Date> getLatestNotificationCreatedAtDate() {
        return latestNotificationCreatedAtDate;
    }

    public final Optional<String> getLatestNotificationCreatedAtString() {
        return latestNotificationCreatedAtString;
    }

    public abstract boolean isEmpty();

}
