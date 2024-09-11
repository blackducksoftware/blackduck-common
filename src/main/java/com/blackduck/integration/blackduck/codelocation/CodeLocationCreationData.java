/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;

public class CodeLocationCreationData<T extends CodeLocationBatchOutput> {
    private final NotificationTaskRange notificationTaskRange;
    private final T output;

    public CodeLocationCreationData(NotificationTaskRange notificationTaskRange, T output) {
        this.notificationTaskRange = notificationTaskRange;
        this.output = output;
    }

    public NotificationTaskRange getNotificationTaskRange() {
        return notificationTaskRange;
    }

    public T getOutput() {
        return output;
    }

}
