/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.hub.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.hub.api.generated.component.ActivityDataView;
import com.synopsys.integration.hub.api.generated.component.ReviewedDetails;
import com.synopsys.integration.hub.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.hub.api.generated.component.VersionBomOriginView;
import com.synopsys.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.hub.api.generated.enumeration.PolicyStatusSummaryStatusType;
import com.synopsys.integration.hub.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.hub.api.generated.enumeration.VersionBomComponentMatchType;
import com.synopsys.integration.hub.api.generated.enumeration.VersionBomComponentReviewStatusType;
import com.synopsys.integration.hub.api.generated.view.MatchedFileView;
import com.synopsys.integration.hub.api.generated.view.VersionBomComponentView;

public class VersionBomComponentModel {
    private final VersionBomComponentView component;
    private final List<MatchedFilesModel> matchedFiles;

    public VersionBomComponentModel(final VersionBomComponentView component, final List<MatchedFileView> matchedFiles) {
        this.component = component;
        this.matchedFiles = getMatchedFilesModel(matchedFiles);
    }

    public ActivityDataView getActivityData() {
        return component.activityData;
    }

    public RiskProfileCounts getActivityRiskProfile() {
        return new RiskProfileCounts(component.activityRiskProfile);
    }

    public boolean hasActivityRisk() {
        return hasRisk(getActivityRiskProfile());
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

    public boolean hasLicenseRisk() {
        return hasRisk(getLicenseRiskProfile());
    }

    public List<VersionBomLicenseView> getLicenses() {
        return component.licenses;
    }

    public RiskProfileCounts getOperationalRiskProfile() {
        return new RiskProfileCounts(component.operationalRiskProfile);
    }

    public boolean hasOperationalRisk() {
        return hasRisk(getOperationalRiskProfile());
    }

    public List<VersionBomOriginView> getOrigins() {
        return component.origins;
    }

    public List<VersionBomComponentMatchType> getMatchTypes() {
        return component.matchTypes;
    }

    public Date getReleasedOn() {
        return component.releasedOn;
    }

    public RiskProfileCounts getSecurityRiskProfile() {
        return new RiskProfileCounts(component.securityRiskProfile);
    }

    public boolean hasSecurityRisk() {
        return hasRisk(getSecurityRiskProfile());
    }

    public List<MatchedFileUsagesType> getUsages() {
        return component.usages;
    }

    public RiskProfileCounts getVersionRiskProfile() {
        return new RiskProfileCounts(component.versionRiskProfile);
    }

    public boolean hasVersionRisk() {
        return hasRisk(getVersionRiskProfile());
    }

    public VersionBomComponentReviewStatusType getReviewStatus() {
        return component.reviewStatus;
    }

    public ReviewedDetails getReviewedDetails() {
        return component.reviewedDetails;
    }

    public PolicyStatusSummaryStatusType getApprovalStatus() {
        return component.approvalStatus;
    }

    public List<MatchedFilesModel> getMatchedFiles() {
        return matchedFiles;
    }

    private boolean hasRisk(final RiskProfileCounts counts) {
        if (counts.getCount(RiskCountType.LOW) + counts.getCount(RiskCountType.MEDIUM) + counts.getCount(RiskCountType.HIGH) > 0) {
            return true;
        }
        return false;
    }

    private List<MatchedFilesModel> getMatchedFilesModel(final List<MatchedFileView> matchedFiles) {
        final List<MatchedFilesModel> matchedFileModels = new ArrayList<>(matchedFiles.size());
        for (final MatchedFileView matchedFile : matchedFiles) {
            matchedFileModels.add(new MatchedFilesModel(matchedFile));
        }
        return matchedFileModels;
    }
}
