/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.blackduck.api.enumeration.PolicySeverityType;
import com.synopsys.integration.blackduck.api.generated.component.NameValuePairView;
import com.synopsys.integration.util.Stringable;

public class ComponentVersionPolicyViolationCount extends Stringable {
    public PolicySeverityType name;
    public int value;

    public ComponentVersionPolicyViolationCount() {
    }

    public ComponentVersionPolicyViolationCount(final NameValuePairView nameValuePair) {
        final Set<PolicySeverityType> policySeverityTypes = EnumSet.allOf(PolicySeverityType.class);
        final Set<String> policyStatusTypeValues = policySeverityTypes.stream().map(Object::toString).collect(Collectors.toSet());
        if (policyStatusTypeValues.contains(nameValuePair.getName())) {
            name = PolicySeverityType.valueOf(nameValuePair.getName());
        }

        if (nameValuePair.getValue() != null) {
            final String valueString = nameValuePair.getValue().toString();
            if (NumberUtils.isCreatable(valueString)) {
                value = NumberUtils.createNumber(valueString).intValue();
            }
        }
    }

}
