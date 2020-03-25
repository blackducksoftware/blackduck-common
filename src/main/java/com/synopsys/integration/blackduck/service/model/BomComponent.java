/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class BomComponent {
    private String policyStatus;
    private List<PolicyRule> policyRulesViolated;
    private String componentName;
    private String componentURL;
    private String componentVersion;
    private String componentVersionURL;
    private String license;
    private BomRiskCounts securityRiskCounts;
    private BomRiskCounts licenseRiskCounts;
    private BomRiskCounts operationalRiskCounts;

    public void addSecurityRiskProfile(RiskProfileView securityRiskProfile) {
        addRiskProfile(securityRiskProfile, securityRiskCounts);
    }

    public void addLicenseRiskProfile(RiskProfileView licenseRiskProfile) {
        addRiskProfile(licenseRiskProfile, licenseRiskCounts);
    }

    public void addOperationalRiskProfile(RiskProfileView operationalRiskProfile) {
        addRiskProfile(operationalRiskProfile, operationalRiskCounts);
    }

    private void addRiskProfile(RiskProfileView riskProfileView, BomRiskCounts bomRiskCounts) {
        if (null == riskProfileView || CollectionUtils.isEmpty(riskProfileView.getCounts())) {
            return;
        }

        riskProfileView
                .getCounts()
                .stream()
                .forEach(bomRiskCounts::add);
    }

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
        return securityRiskCounts.getHigh();
    }

    public int getSecurityRiskMediumCount() {
        return securityRiskCounts.getMedium();
    }

    public int getSecurityRiskLowCount() {
        return securityRiskCounts.getLow();
    }

    public int getLicenseRiskHighCount() {
        return licenseRiskCounts.getHigh();
    }

    public int getLicenseRiskMediumCount() {
        return licenseRiskCounts.getMedium();
    }

    public int getLicenseRiskLowCount() {
        return licenseRiskCounts.getLow();
    }

    public int getOperationalRiskHighCount() {
        return operationalRiskCounts.getHigh();
    }

    public int getOperationalRiskMediumCount() {
        return operationalRiskCounts.getMedium();
    }

    public int getOperationalRiskLowCount() {
        return operationalRiskCounts.getLow();
    }

    public BomRiskCounts getSecurityRiskCounts() {
        return securityRiskCounts;
    }

    public BomRiskCounts getLicenseRiskCounts() {
        return licenseRiskCounts;
    }

    public BomRiskCounts getOperationalRiskCounts() {
        return operationalRiskCounts;
    }

}
