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
package com.blackducksoftware.integration.hub.report;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;

public class ReportData {
    private String projectName;

    private String projectURL;

    private String projectVersion;

    private String projectVersionURL;

    private PhaseEnum phase;

    private DistributionEnum distribution;

    private List<BomComponent> components;

    private int totalComponents;

    private int vulnerabilityRiskHighCount;

    private int vulnerabilityRiskMediumCount;

    private int vulnerabilityRiskLowCount;

    private int vulnerabilityRiskNoneCount;

    private int licenseRiskHighCount;

    private int licenseRiskMediumCount;

    private int licenseRiskLowCount;

    private int licenseRiskNoneCount;

    private int operationalRiskHighCount;

    private int operationalRiskMediumCount;

    private int operationalRiskLowCount;

    private int operationalRiskNoneCount;

    public void setComponents(final List<BomComponent> components) {
        this.components = components;

        vulnerabilityRiskHighCount = 0;
        vulnerabilityRiskMediumCount = 0;
        vulnerabilityRiskLowCount = 0;

        licenseRiskHighCount = 0;
        licenseRiskMediumCount = 0;
        licenseRiskLowCount = 0;

        operationalRiskHighCount = 0;
        operationalRiskMediumCount = 0;
        operationalRiskLowCount = 0;

        for (final BomComponent component : components) {
            if (component != null) {
                if (component.getSecurityRiskHighCount() > 0) {
                    vulnerabilityRiskHighCount++;
                } else if (component.getSecurityRiskMediumCount() > 0) {
                    vulnerabilityRiskMediumCount++;
                } else if (component.getSecurityRiskLowCount() > 0) {
                    vulnerabilityRiskLowCount++;
                }
                if (component.getLicenseRiskHighCount() > 0) {
                    licenseRiskHighCount++;
                } else if (component.getLicenseRiskMediumCount() > 0) {
                    licenseRiskMediumCount++;
                } else if (component.getLicenseRiskLowCount() > 0) {
                    licenseRiskLowCount++;
                }
                if (component.getOperationalRiskHighCount() > 0) {
                    operationalRiskHighCount++;
                } else if (component.getOperationalRiskMediumCount() > 0) {
                    operationalRiskMediumCount++;
                } else if (component.getOperationalRiskLowCount() > 0) {
                    operationalRiskLowCount++;
                }
            }
        }
        totalComponents = components.size();

        vulnerabilityRiskNoneCount = totalComponents - vulnerabilityRiskHighCount - vulnerabilityRiskMediumCount - vulnerabilityRiskLowCount;
        licenseRiskNoneCount = totalComponents - licenseRiskHighCount - licenseRiskMediumCount - licenseRiskLowCount;
        operationalRiskNoneCount = totalComponents - operationalRiskHighCount - operationalRiskMediumCount - operationalRiskLowCount;
    }

    public String htmlEscape(final String valueToEscape) {
        if (StringUtils.isBlank(valueToEscape)) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(valueToEscape);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getProjectURL() {
        return projectURL;
    }

    public void setProjectURL(final String projectURL) {
        this.projectURL = projectURL;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(final String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getProjectVersionURL() {
        return projectVersionURL;
    }

    public void setProjectVersionURL(final String projectVersionURL) {
        this.projectVersionURL = projectVersionURL;
    }

    public PhaseEnum getPhase() {
        return phase;
    }

    public void setPhase(final PhaseEnum phase) {
        this.phase = phase;
    }

    public DistributionEnum getDistribution() {
        return distribution;
    }

    public void setDistribution(final DistributionEnum distribution) {
        this.distribution = distribution;
    }

    public int getTotalComponents() {
        return totalComponents;
    }

    public int getVulnerabilityRiskHighCount() {
        return vulnerabilityRiskHighCount;
    }

    public int getVulnerabilityRiskMediumCount() {
        return vulnerabilityRiskMediumCount;
    }

    public int getVulnerabilityRiskLowCount() {
        return vulnerabilityRiskLowCount;
    }

    public int getVulnerabilityRiskNoneCount() {
        return vulnerabilityRiskNoneCount;
    }

    public int getLicenseRiskHighCount() {
        return licenseRiskHighCount;
    }

    public int getLicenseRiskMediumCount() {
        return licenseRiskMediumCount;
    }

    public int getLicenseRiskLowCount() {
        return licenseRiskLowCount;
    }

    public int getLicenseRiskNoneCount() {
        return licenseRiskNoneCount;
    }

    public int getOperationalRiskHighCount() {
        return operationalRiskHighCount;
    }

    public int getOperationalRiskMediumCount() {
        return operationalRiskMediumCount;
    }

    public int getOperationalRiskLowCount() {
        return operationalRiskLowCount;
    }

    public int getOperationalRiskNoneCount() {
        return operationalRiskNoneCount;
    }

    public List<BomComponent> getComponents() {
        return components;
    }

}
