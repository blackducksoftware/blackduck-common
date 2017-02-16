/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.report;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;

public class BomComponent {
    private PolicyStatusEnum policyStatus;

    private String componentName;

    private String componentURL;

    private String componentVersion;

    private String componentVersionURL;

    private String license;

    private int highSecurityRisk;

    private int mediumSecurityRisk;

    private int lowSecurityRisk;

    private int highLicenseRisk;

    private int mediumLicenseRisk;

    private int lowLicenseRisk;

    private int highOperationalRisk;

    private int mediumOperationalRisk;

    private int lowOperationalRisk;

    public PolicyStatusEnum getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final PolicyStatusEnum policyStatus) {
        this.policyStatus = policyStatus;
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

    public int getHighSecurityRisk() {
        return highSecurityRisk;
    }

    public void setHighSecurityRisk(final int highSecurityRisk) {
        this.highSecurityRisk = highSecurityRisk;
    }

    public int getMediumSecurityRisk() {
        return mediumSecurityRisk;
    }

    public void setMediumSecurityRisk(final int mediumSecurityRisk) {
        this.mediumSecurityRisk = mediumSecurityRisk;
    }

    public int getLowSecurityRisk() {
        return lowSecurityRisk;
    }

    public void setLowSecurityRisk(final int lowSecurityRisk) {
        this.lowSecurityRisk = lowSecurityRisk;
    }

    public int getHighLicenseRisk() {
        return highLicenseRisk;
    }

    public void setHighLicenseRisk(final int highLicenseRisk) {
        this.highLicenseRisk = highLicenseRisk;
    }

    public int getMediumLicenseRisk() {
        return mediumLicenseRisk;
    }

    public void setMediumLicenseRisk(final int mediumLicenseRisk) {
        this.mediumLicenseRisk = mediumLicenseRisk;
    }

    public int getLowLicenseRisk() {
        return lowLicenseRisk;
    }

    public void setLowLicenseRisk(final int lowLicenseRisk) {
        this.lowLicenseRisk = lowLicenseRisk;
    }

    public int getHighOperationalRisk() {
        return highOperationalRisk;
    }

    public void setHighOperationalRisk(final int highOperationalRisk) {
        this.highOperationalRisk = highOperationalRisk;
    }

    public int getMediumOperationalRisk() {
        return mediumOperationalRisk;
    }

    public void setMediumOperationalRisk(final int mediumOperationalRisk) {
        this.mediumOperationalRisk = mediumOperationalRisk;
    }

    public int getLowOperationalRisk() {
        return lowOperationalRisk;
    }

    public void setLowOperationalRisk(final int lowOperationalRisk) {
        this.lowOperationalRisk = lowOperationalRisk;
    }

}
