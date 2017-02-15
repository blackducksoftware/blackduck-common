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

import java.util.List;

public class ComponentLicense {

    private String licenseDisplay;

    private ComponentLicenseType licenseType;

    private String license;

    private List<ComponentLicense> licenses;

    public String getLicenseDisplay() {
        return licenseDisplay;
    }

    public ComponentLicenseType getLicenseType() {
        return licenseType;
    }

    public String getLicense() {
        return license;
    }

    public List<ComponentLicense> getLicenses() {
        return licenses;
    }

}
