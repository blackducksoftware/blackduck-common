/**
 * hub-common
 * <p>
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.List;

import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityV2View;

public class ComponentVersionVulnerabilities {
    private final ComponentVersionView componentVersionView;
    private final List<VulnerabilityV2View> vulnerabilities;

    public ComponentVersionVulnerabilities(final ComponentVersionView componentVersionView, final List<VulnerabilityV2View> vulnerabilities) {
        this.componentVersionView = componentVersionView;
        this.vulnerabilities = vulnerabilities;
    }

    public ComponentVersionView getComponentVersionView() {
        return componentVersionView;
    }

    public List<VulnerabilityV2View> getVulnerabilities() {
        return vulnerabilities;
    }

}
