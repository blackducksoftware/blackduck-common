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
package com.blackducksoftware.integration.hub.api.aggregate.bom;

import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.api.aggregate.bom.risk.RiskProfile;
import com.blackducksoftware.integration.hub.api.item.HubItem;

public class ComponentItem extends HubItem {
    private String componentName;

    private String componentVersionName;

    private String component;

    private String componentVersion;

    private List<ComponentLicense> licenses;

    private Date releasedOn;

    private RiskProfile licenseRiskProfile;

    private RiskProfile securityRiskProfile;

    private RiskProfile versionRiskProfile;

    private RiskProfile activityRiskProfile;

    private RiskProfile operationalRiskProfile;

    private ComponentActivityData activityData;

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getComponent() {
        return component;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public List<ComponentLicense> getLicenses() {
        return licenses;
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public RiskProfile getLicenseRiskProfile() {
        return licenseRiskProfile;
    }

    public RiskProfile getSecurityRiskProfile() {
        return securityRiskProfile;
    }

    public RiskProfile getVersionRiskProfile() {
        return versionRiskProfile;
    }

    public RiskProfile getActivityRiskProfile() {
        return activityRiskProfile;
    }

    public RiskProfile getOperationalRiskProfile() {
        return operationalRiskProfile;
    }

    public ComponentActivityData getActivityData() {
        return activityData;
    }

}
