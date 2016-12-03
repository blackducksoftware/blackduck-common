/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.policy;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class PolicyRule extends HubItem {
    public static final String POLICY_RULES_URL_IDENTIFIER = "policy-rules";

    private final String name;

    private final String description;

    private final Boolean enabled;

    private final Boolean overridable;

    private final PolicyExpressions expression;

    private final String createdAt;

    private final String createdBy;

    private final String updatedAt;

    private final String updatedBy;

    public PolicyRule(final MetaInformation meta, final String name, final String description, final Boolean enabled,
            final Boolean overridable, final PolicyExpressions expression, final String createdAt,
            final String createdBy, final String updatedAt, final String updatedBy) {
        super(meta);
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.overridable = overridable;
        this.expression = expression;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getOverridable() {
        return overridable;
    }

    public PolicyExpressions getExpression() {
        return expression;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public DateTime getUpdatedAtTime() {
        return getDateTime(updatedAt);
    }

    public DateTime getCreatedAtTime() {
        return getDateTime(createdAt);
    }

}
