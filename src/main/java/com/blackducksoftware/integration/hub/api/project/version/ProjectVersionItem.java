/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.api.project.version;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.component.version.ComplexLicense;
import com.blackducksoftware.integration.hub.api.component.version.SourceEnum;
import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;

public class ProjectVersionItem extends HubItem {
    private DistributionEnum distribution;

    private ComplexLicense license;

    private String nickname;

    private PhaseEnum phase;

    private String releaseComments;

    private Date releasedOn;

    // description from Hub API: "Read-Only; No matter the value it will always default to 'CUSTOM'",
    private SourceEnum source;

    private String versionName;

    public DistributionEnum getDistribution() {
        return distribution;
    }

    public ComplexLicense getLicense() {
        return license;
    }

    public String getNickname() {
        return nickname;
    }

    public PhaseEnum getPhase() {
        return phase;
    }

    public String getReleaseComments() {
        return releaseComments;
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public SourceEnum getSource() {
        return source;
    }

    public String getVersionName() {
        return versionName;
    }

}
