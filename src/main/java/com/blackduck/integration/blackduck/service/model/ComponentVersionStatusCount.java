/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.manual.temporary.component.NameValuePairView;
import com.blackduck.integration.util.Stringable;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

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
