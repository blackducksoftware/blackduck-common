/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionActivityDataView;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionReviewedDetailsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentReviewStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.VersionBomOriginView;

public class VersionBomComponentModel {
    private final ProjectVersionComponentVersionView component;
    private final List<MatchedFilesModel> matchedFiles;

    public VersionBomComponentModel(ProjectVersionComponentVersionView component, List<ComponentMatchedFilesView> matchedFiles) {
        this.component = component;
        this.matchedFiles = getMatchedFilesModel(matchedFiles);
    }

    public ProjectVersionComponentVersionActivityDataView getActivityData() {
        return component.getActivityData();
    }

    public RiskProfileCounts getActivityRiskProfile() {
        return new RiskProfileCounts(component.getActivityRiskProfile());
    }

    public boolean hasActivityRisk() {
        return hasRisk(getActivityRiskProfile());
    }

    public String getComponent() {
        return component.getComponent();
    }

    public String getComponentName() {
        return component.getComponentName();
    }

    public String getComponentVersion() {
        return component.getComponentVersion();
    }

    public String getComponentVersionName() {
        return component.getComponentVersionName();
    }

    public RiskProfileCounts getLicenseRiskProfile() {
        return new RiskProfileCounts(component.getLicenseRiskProfile());
    }

    public boolean hasLicenseRisk() {
        return hasRisk(getLicenseRiskProfile());
    }

    public List<ProjectVersionComponentVersionLicensesView> getLicenses() {
        return component.getLicenses();
    }

    public RiskProfileCounts getOperationalRiskProfile() {
        return new RiskProfileCounts(component.getOperationalRiskProfile());
    }

    public boolean hasOperationalRisk() {
        return hasRisk(getOperationalRiskProfile());
    }

    public List<VersionBomOriginView> getOrigins() {
        return component.getOrigins();
    }

    public List<MatchType> getMatchTypes() {
        return component.getMatchTypes();
    }

    public Date getReleasedOn() {
        return component.getReleasedOn();
    }

    public RiskProfileCounts getSecurityRiskProfile() {
        return new RiskProfileCounts(component.getSecurityRiskProfile());
    }

    public boolean hasSecurityRisk() {
        return hasRisk(getSecurityRiskProfile());
    }

    public List<UsageType> getUsages() {
        return component.getUsages();
    }

    public RiskProfileCounts getVersionRiskProfile() {
        return new RiskProfileCounts(component.getVersionRiskProfile());
    }

    public boolean hasVersionRisk() {
        return hasRisk(getVersionRiskProfile());
    }

    public ProjectVersionComponentReviewStatusType getReviewStatus() {
        return component.getReviewStatus();
    }

    public ProjectVersionComponentVersionReviewedDetailsView getReviewedDetails() {
        return component.getReviewedDetails();
    }

    public ProjectVersionComponentPolicyStatusType getApprovalStatus() {
        return component.getApprovalStatus();
    }

    public List<MatchedFilesModel> getMatchedFiles() {
        return matchedFiles;
    }

    private boolean hasRisk(RiskProfileCounts counts) {
        return counts.getCount(RiskPriorityType.LOW).intValue() + counts.getCount(RiskPriorityType.MEDIUM).intValue() + counts.getCount(
            RiskPriorityType.HIGH).intValue() > 0;
    }

    private List<MatchedFilesModel> getMatchedFilesModel(List<ComponentMatchedFilesView> matchedFiles) {
        List<MatchedFilesModel> matchedFileModels = new ArrayList<>(matchedFiles.size());
        for (ComponentMatchedFilesView matchedFile : matchedFiles) {
            matchedFileModels.add(new MatchedFilesModel(matchedFile));
        }
        return matchedFileModels;
    }

}
