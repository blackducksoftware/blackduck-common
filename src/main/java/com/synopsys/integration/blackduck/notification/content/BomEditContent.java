/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.blackduck.notification.content;

import java.util.Collections;
import java.util.List;

import com.synopsys.integration.blackduck.service.model.ProjectVersionDescription;

public class BomEditContent extends NotificationContent {
    public String bomComponent;

    @Override
    public boolean providesPolicyDetails() {
        return false;
    }

    @Override
    public boolean providesVulnerabilityDetails() {
        return false;
    }

    @Override
    public boolean providesProjectComponentDetails() {
        return false;
    }

    @Override
    public boolean providesLicenseDetails() {
        return false;
    }

    @Override
    public List<ProjectVersionDescription> getAffectedProjectVersionDescriptions() {
        return Collections.emptyList();
    }

}
