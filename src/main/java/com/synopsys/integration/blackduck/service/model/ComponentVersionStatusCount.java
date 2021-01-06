/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.manual.temporary.component.NameValuePairView;
import com.synopsys.integration.util.Stringable;

public class ComponentVersionStatusCount extends Stringable {
    public ProjectVersionComponentPolicyStatusType name;
    public int value;

    public ComponentVersionStatusCount() {
    }

    public ComponentVersionStatusCount(NameValuePairView nameValuePair) {
        Set<ProjectVersionComponentPolicyStatusType> policyStatusTypes = EnumSet.allOf(ProjectVersionComponentPolicyStatusType.class);
        Set<String> policyStatusTypeValues = policyStatusTypes.stream().map(Object::toString).collect(Collectors.toSet());
        if (policyStatusTypeValues.contains(nameValuePair.getName())) {
            name = ProjectVersionComponentPolicyStatusType.valueOf(nameValuePair.getName());
        }

        if (nameValuePair.getValue() != null) {
            String valueString = nameValuePair.getValue().toString();
            if (NumberUtils.isCreatable(valueString)) {
                value = NumberUtils.createNumber(valueString).intValue();
            }
        }
    }

}
