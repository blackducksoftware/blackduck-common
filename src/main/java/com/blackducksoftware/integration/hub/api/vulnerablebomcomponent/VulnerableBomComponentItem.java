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
package com.blackducksoftware.integration.hub.api.vulnerablebomcomponent;

import com.blackducksoftware.integration.hub.model.view.HubView;
import com.google.gson.annotations.SerializedName;

public class VulnerableBomComponentItem extends HubView {
    private String componentName;

    private String componentVersionName;

    @SerializedName("componentVersion")
    private String componentVersionLink;

    private VulnerabilityWithRemediation vulnerabilityWithRemediation;

    // Also in Hub's response: License

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public void setComponentVersionName(final String componentVersionName) {
        this.componentVersionName = componentVersionName;
    }

    public String getComponentVersionLink() {
        return componentVersionLink;
    }

    public void setComponentVersionLink(final String componentVersionLink) {
        this.componentVersionLink = componentVersionLink;
    }

    public VulnerabilityWithRemediation getVulnerabilityWithRemediation() {
        return vulnerabilityWithRemediation;
    }

    public void setVulnerabilityWithRemediation(final VulnerabilityWithRemediation vulnerabilityWithRemediation) {
        this.vulnerabilityWithRemediation = vulnerabilityWithRemediation;
    }

}
