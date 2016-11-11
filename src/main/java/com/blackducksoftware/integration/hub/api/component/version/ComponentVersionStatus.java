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
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.util.HubUrlParser;
import com.google.gson.annotations.SerializedName;

public class ComponentVersionStatus {
    public static final String COMPONENT_URL_IDENTIFIER = "components";

    public static final String COMPONENT_VERSION_URL_IDENTIFIER = "versions";

    private String componentName;

    // If version is specified, componentVersionLink will be populated
    // otherwise it will be null
    @SerializedName("componentVersion")
    private String componentVersionLink;

    // If version is not specified, componentLink will be populated
    // otherwise it will be null
    @SerializedName("component")
    private String componentLink;

    @SerializedName("bomComponentVersionPolicyStatus")
    private String bomComponentVersionPolicyStatusLink;

    private List<String> policies;

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionLink() {
        return componentVersionLink;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public String getBomComponentVersionPolicyStatusLink() {
        return bomComponentVersionPolicyStatusLink;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public void setComponentVersionLink(final String componentVersionLink) {
        this.componentVersionLink = componentVersionLink;
    }

    public void setBomComponentVersionPolicyStatusLink(final String bomComponentVersionPolicyStatusLink) {
        this.bomComponentVersionPolicyStatusLink = bomComponentVersionPolicyStatusLink;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(final List<String> policies) {
        this.policies = policies;
    }

    @Deprecated
    public UUID getComponentId() throws MissingUUIDException {
        if (StringUtils.isBlank(getComponentVersionLink())
                && StringUtils.isBlank(getBomComponentVersionPolicyStatusLink())) {
            return null;
        }
        if (StringUtils.isNotBlank(getComponentVersionLink())) {
            return HubUrlParser.getUUIDFromURLString(COMPONENT_URL_IDENTIFIER, getComponentVersionLink());
        } else {
            return HubUrlParser.getUUIDFromURLString(COMPONENT_URL_IDENTIFIER,
                    getBomComponentVersionPolicyStatusLink());
        }
    }

    @Deprecated
    public UUID getComponentVersionId() throws MissingUUIDException {
        if (StringUtils.isBlank(getComponentVersionLink())) {
            return null;
        }
        return HubUrlParser.getUUIDFromURLString(COMPONENT_VERSION_URL_IDENTIFIER, getComponentVersionLink());
    }

    @Override
    public String toString() {
        return "ComponentVersionStatus [componentName=" + componentName + ", componentVersionLink="
                + componentVersionLink + ", bomComponentVersionPolicyStatusLink=" + bomComponentVersionPolicyStatusLink
                + ", policies=" + policies + "]";
    }
}
