/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;

public class BomComponent {
    private String policyStatus;
    private List<PolicyRule> policyRulesViolated;
    private String componentName;
    private String componentURL;
    private String componentVersion;
    private String componentVersionURL;
    private String license;
    private BomRiskCounts securityRiskCounts = new BomRiskCounts();
    private BomRiskCounts licenseRiskCounts = new BomRiskCounts();
    private BomRiskCounts operationalRiskCounts = new BomRiskCounts();

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

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public List<PolicyRule> getPolicyRulesViolated() {
        return policyRulesViolated;
    }

    public void setPolicyRulesViolated(List<PolicyRule> policyRulesViolated) {
        this.policyRulesViolated = policyRulesViolated;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentURL() {
        return componentURL;
    }

    public void setComponentURL(String componentURL) {
        this.componentURL = componentURL;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public void setComponentVersion(String componentVersion) {
        this.componentVersion = componentVersion;
    }

    public String getComponentVersionURL() {
        return componentVersionURL;
    }

    public void setComponentVersionURL(String componentVersionURL) {
        this.componentVersionURL = componentVersionURL;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
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
