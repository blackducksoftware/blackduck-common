/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.binaryscanner;

import com.blackduck.integration.util.NameVersion;

import java.io.File;

public class BinaryScan {
    private final File binaryFile;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public BinaryScan(File binaryFile, String projectName, String projectVersion, String codeLocationName) {
        this.binaryFile = binaryFile;
        this.projectAndVersion = new NameVersion(projectName, projectVersion);
        this.codeLocationName = codeLocationName;
    }

    public File getBinaryFile() {
        return binaryFile;
    }

    public NameVersion getProjectAndVersion() {
        return projectAndVersion;
    }

    public String getProjectName() {
        return projectAndVersion.getName();
    }

    public String getProjectVersion() {
        return projectAndVersion.getVersion();
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

}
