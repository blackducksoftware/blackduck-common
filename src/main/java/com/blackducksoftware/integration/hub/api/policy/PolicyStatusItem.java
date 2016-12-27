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
package com.blackducksoftware.integration.hub.api.policy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.item.HubItem;

public class PolicyStatusItem extends HubItem {
    private PolicyStatusEnum overallStatus;

    private String updatedAt;

    private List<ComponentVersionStatusCount> componentVersionStatusCounts;

    public ComponentVersionStatusCount getCountInViolation() {
        if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
            return null;
        }
        for (ComponentVersionStatusCount count : componentVersionStatusCounts) {
            if (PolicyStatusEnum.IN_VIOLATION == count.getName()) {
                return count;
            }
        }
        return null;
    }

    public ComponentVersionStatusCount getCountNotInViolation() {
        if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
            return null;
        }
        for (ComponentVersionStatusCount count : componentVersionStatusCounts) {
            if (PolicyStatusEnum.NOT_IN_VIOLATION == count.getName()) {
                return count;
            }
        }
        return null;
    }

    public ComponentVersionStatusCount getCountInViolationOverridden() {
        if (componentVersionStatusCounts == null || componentVersionStatusCounts.isEmpty()) {
            return null;
        }
        for (ComponentVersionStatusCount count : componentVersionStatusCounts) {
            if (PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN == count.getName()) {
                return count;
            }
        }
        return null;
    }

    public DateTime getUpdatedAtTime() {
        if (StringUtils.isBlank(updatedAt)) {
            return null;
        }
        try {
            return new DateTime(updatedAt);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public PolicyStatusEnum getOverallStatus() {
        return overallStatus;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<ComponentVersionStatusCount> getComponentVersionStatusCounts() {
        return componentVersionStatusCounts;
    }

}
