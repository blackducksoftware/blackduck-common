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
package com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.dataservice.model.RiskProfileCounts;
import com.blackducksoftware.integration.hub.model.enumeration.MatchTypeEnum;
import com.blackducksoftware.integration.hub.model.enumeration.MatchedFileUsageEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ReviewStatusEnum;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.model.view.components.ActivityDataView;
import com.blackducksoftware.integration.hub.model.view.components.OriginView;
import com.blackducksoftware.integration.hub.model.view.components.ReviewedDetailsView;
import com.blackducksoftware.integration.hub.model.view.components.VersionBomLicenseView;

public class VersionBomComponentModel {
    private final VersionBomComponentView component;
    private final List<MatchedFilesModel> matchedFiles;

    public VersionBomComponentModel(final VersionBomComponentView component, final List<MatchedFilesView> matchedFiles) {
        this.component = component;
        this.matchedFiles = getMatchedFilesModel(matchedFiles);
    }

    public ActivityDataView getActivityData() {
        return component.activityData;
    }

    public RiskProfileCounts getActivityRiskProfile() {
        return new RiskProfileCounts(component.activityRiskProfile);
    }

    public String getComponent() {
        return component.component;
    }

    public String getComponentName() {
        return component.componentName;
    }

    public String getComponentVersion() {
        return component.componentVersion;
    }

    public String getComponentVersionName() {
        return component.componentVersionName;
    }

    public RiskProfileCounts getLicenseRiskProfile() {
        return new RiskProfileCounts(component.licenseRiskProfile);
    }

    public List<VersionBomLicenseView> getLicenses() {
        return component.licenses;
    }

    public RiskProfileCounts getOperationalRiskProfile() {
        return new RiskProfileCounts(component.operationalRiskProfile);
    }

    public List<OriginView> getOrigins() {
        return component.origins;
    }

    public List<MatchTypeEnum> getMatchTypes() {
        return component.matchTypes;
    }

    public Date getReleasedOn() {
        return component.releasedOn;
    }

    public RiskProfileCounts getSecurityRiskProfile() {
        return new RiskProfileCounts(component.securityRiskProfile);
    }

    public List<MatchedFileUsageEnum> getUsages() {
        return component.usages;
    }

    public RiskProfileCounts getVersionRiskProfile() {
        return new RiskProfileCounts(component.versionRiskProfile);
    }

    public ReviewStatusEnum getReviewStatus() {
        return component.reviewStatus;
    }

    public ReviewedDetailsView getReviewedDetails() {
        return component.reviewedDetails;
    }

    public String getApprovalStatus() {
        return component.approvalStatus;
    }

    public List<MatchedFilesModel> getMatchedFiles() {
        return matchedFiles;
    }

    private List<MatchedFilesModel> getMatchedFilesModel(final List<MatchedFilesView> matchedFiles) {
        final List<MatchedFilesModel> matchedFileModels = new ArrayList<>(matchedFiles.size());
        for (final MatchedFilesView matchedFile : matchedFiles) {
            matchedFileModels.add(new MatchedFilesModel(matchedFile));
        }
        return matchedFileModels;
    }
}
