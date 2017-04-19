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
package com.blackducksoftware.integration.hub.dataservice.model;

import java.util.Date;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionSourceEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;

public class ProjectVersionModel {
    private String projectName;

    private String projectVersionName;

    private String url;

    private ProjectVersionDistributionEnum distribution;

    private ComplexLicenseView license;

    private String nickname;

    private ProjectVersionPhaseEnum phase;

    private String releaseComments;

    private Date releasedOn;

    // description from Hub API: "Read-Only; No matter the value it will always default to 'CUSTOM'",
    private ProjectVersionSourceEnum source;

    private String versionName;

    private String versionReportLink;

    private String riskProfileLink;

    private String componentsLink;

    private String vulnerableComponentsLink;

    private String projectLink;

    private String policyStatusLink;

    private String codeLocationsLink;

    private String componentIssueLink;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public void setProjectVersionName(final String projectVersionName) {
        this.projectVersionName = projectVersionName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public ProjectVersionDistributionEnum getDistribution() {
        return distribution;
    }

    public void setDistribution(final ProjectVersionDistributionEnum distribution) {
        this.distribution = distribution;
    }

    public ComplexLicenseView getLicense() {
        return license;
    }

    public void setLicense(final ComplexLicenseView license) {
        this.license = license;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public ProjectVersionPhaseEnum getPhase() {
        return phase;
    }

    public void setPhase(final ProjectVersionPhaseEnum phase) {
        this.phase = phase;
    }

    public String getReleaseComments() {
        return releaseComments;
    }

    public void setReleaseComments(final String releaseComments) {
        this.releaseComments = releaseComments;
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public void setReleasedOn(final Date releasedOn) {
        this.releasedOn = releasedOn;
    }

    public ProjectVersionSourceEnum getSource() {
        return source;
    }

    public void setSource(final ProjectVersionSourceEnum source) {
        this.source = source;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public String getVersionReportLink() {
        return versionReportLink;
    }

    public void setVersionReportLink(final String versionReportLink) {
        this.versionReportLink = versionReportLink;
    }

    public String getRiskProfileLink() {
        return riskProfileLink;
    }

    public void setRiskProfileLink(final String riskProfileLink) {
        this.riskProfileLink = riskProfileLink;
    }

    public String getComponentsLink() {
        return componentsLink;
    }

    public void setComponentsLink(final String componentsLink) {
        this.componentsLink = componentsLink;
    }

    public String getVulnerableComponentsLink() {
        return vulnerableComponentsLink;
    }

    public void setVulnerableComponentsLink(final String vulnerableComponentsLink) {
        this.vulnerableComponentsLink = vulnerableComponentsLink;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(final String projectLink) {
        this.projectLink = projectLink;
    }

    public String getPolicyStatusLink() {
        return policyStatusLink;
    }

    public void setPolicyStatusLink(final String policyStatusLink) {
        this.policyStatusLink = policyStatusLink;
    }

    public String getCodeLocationsLink() {
        return codeLocationsLink;
    }

    public void setCodeLocationsLink(final String codeLocationsLink) {
        this.codeLocationsLink = codeLocationsLink;
    }

    public String getComponentIssueLink() {
        return componentIssueLink;
    }

    public void setComponentIssueLink(final String componentIssueLink) {
        this.componentIssueLink = componentIssueLink;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
