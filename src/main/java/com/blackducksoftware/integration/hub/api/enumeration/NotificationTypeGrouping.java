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
package com.blackducksoftware.integration.hub.api.enumeration;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public enum NotificationTypeGrouping {
    BOM,
    LICENSE,
    POLICY,
    VULNERABILITY;

    private static final Map<NotificationType, NotificationTypeGrouping> typeToGrouping = new HashMap<>();
    static {
        typeToGrouping.put(NotificationType.LICENSE_LIMIT, NotificationTypeGrouping.LICENSE);
        typeToGrouping.put(NotificationType.POLICY_OVERRIDE, NotificationTypeGrouping.POLICY);
        typeToGrouping.put(NotificationType.RULE_VIOLATION, NotificationTypeGrouping.POLICY);
        typeToGrouping.put(NotificationType.RULE_VIOLATION_CLEARED, NotificationTypeGrouping.POLICY);
        typeToGrouping.put(NotificationType.VULNERABILITY, NotificationTypeGrouping.VULNERABILITY);
        // TODO BOM_EDIT to BOM
    }

    public String getContentKey() {
        return this.name().toLowerCase();
    }

    public static NotificationTypeGrouping fromNotificationType(final NotificationType type) {
        final NotificationTypeGrouping grouping = typeToGrouping.get(type);
        if (grouping == null) {
            throw new IllegalArgumentException("Cannot convert '" + type.name() + "' to " + NotificationTypeGrouping.class.getSimpleName());
        }
        return grouping;
    }

}
