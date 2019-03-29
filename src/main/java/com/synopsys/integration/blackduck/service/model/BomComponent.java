/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.util.List;

public class BomComponent {
    private String policyStatus;
    private List<PolicyRule> policyRulesViolated;
    private String componentName;
    private String componentURL;
    private String componentVersion;
    private String componentVersionURL;
    private String license;
    private int securityRiskHighCount;
    private int securityRiskMediumCount;
    private int securityRiskLowCount;
    private int licenseRiskHighCount;
    private int licenseRiskMediumCount;
    private int licenseRiskLowCount;
    private int operationalRiskHighCount;
    private int operationalRiskMediumCount;
    private int operationalRiskLowCount;

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public List<PolicyRule> getPolicyRulesViolated() {
        return policyRulesViolated;
    }

    public void setPolicyRulesViolated(final List<PolicyRule> policyRulesViolated) {
        this.policyRulesViolated = policyRulesViolated;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public String getComponentURL() {
        return componentURL;
    }

    public void setComponentURL(final String componentURL) {
        this.componentURL = componentURL;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public void setComponentVersion(final String componentVersion) {
        this.componentVersion = componentVersion;
    }

    public String getComponentVersionURL() {
        return componentVersionURL;
    }

    public void setComponentVersionURL(final String componentVersionURL) {
        this.componentVersionURL = componentVersionURL;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(final String license) {
        this.license = license;
    }

    public int getSecurityRiskHighCount() {
        return securityRiskHighCount;
    }

    public void setSecurityRiskHighCount(final int securityRiskHighCount) {
        this.securityRiskHighCount = securityRiskHighCount;
    }

    public int getSecurityRiskMediumCount() {
        return securityRiskMediumCount;
    }

    public void setSecurityRiskMediumCount(final int securityRiskMediumCount) {
        this.securityRiskMediumCount = securityRiskMediumCount;
    }

    public int getSecurityRiskLowCount() {
        return securityRiskLowCount;
    }

    public void setSecurityRiskLowCount(final int securityRiskLowCount) {
        this.securityRiskLowCount = securityRiskLowCount;
    }

    public int getLicenseRiskHighCount() {
        return licenseRiskHighCount;
    }

    public void setLicenseRiskHighCount(final int licenseRiskHighCount) {
        this.licenseRiskHighCount = licenseRiskHighCount;
    }

    public int getLicenseRiskMediumCount() {
        return licenseRiskMediumCount;
    }

    public void setLicenseRiskMediumCount(final int licenseRiskMediumCount) {
        this.licenseRiskMediumCount = licenseRiskMediumCount;
    }

    public int getLicenseRiskLowCount() {
        return licenseRiskLowCount;
    }

    public void setLicenseRiskLowCount(final int licenseRiskLowCount) {
        this.licenseRiskLowCount = licenseRiskLowCount;
    }

    public int getOperationalRiskHighCount() {
        return operationalRiskHighCount;
    }

    public void setOperationalRiskHighCount(final int operationalRiskHighCount) {
        this.operationalRiskHighCount = operationalRiskHighCount;
    }

    public int getOperationalRiskMediumCount() {
        return operationalRiskMediumCount;
    }

    public void setOperationalRiskMediumCount(final int operationalRiskMediumCount) {
        this.operationalRiskMediumCount = operationalRiskMediumCount;
    }

    public int getOperationalRiskLowCount() {
        return operationalRiskLowCount;
    }

    public void setOperationalRiskLowCount(final int operationalRiskLowCount) {
        this.operationalRiskLowCount = operationalRiskLowCount;
    }

}
