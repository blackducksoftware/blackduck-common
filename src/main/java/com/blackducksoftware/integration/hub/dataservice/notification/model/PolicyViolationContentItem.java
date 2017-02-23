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
package com.blackducksoftware.integration.hub.dataservice.notification.model;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;

public class PolicyViolationContentItem extends PolicyContentItem {
    private final List<PolicyRule> policyRuleList;

    public PolicyViolationContentItem(final Date createdAt, final ProjectVersionModel projectVersion,
            final String componentName,
            final ComponentVersion componentVersion, final String componentUrl,
            final String componentVersionUrl,
            final List<PolicyRule> policyRuleList) throws URISyntaxException {
        super(createdAt, projectVersion, componentName, componentVersion, componentUrl, componentVersionUrl);
        this.policyRuleList = policyRuleList;
    }

    public List<PolicyRule> getPolicyRuleList() {
        return policyRuleList;
    }

}
