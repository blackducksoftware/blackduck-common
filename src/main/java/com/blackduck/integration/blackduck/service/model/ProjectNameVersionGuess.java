/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

public class ProjectNameVersionGuess {
    private final String projectName;
    private final String versionName;

    public ProjectNameVersionGuess(final String projectName, final String versionName) {
        this.projectName = projectName;
        this.versionName = versionName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

}
