/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;

import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;

public class ExistingScannerInstaller implements ScannerInstaller {
    private final File existingInstallDirectory;

    public ExistingScannerInstaller(File existingInstallDirectory) {
        this.existingInstallDirectory = existingInstallDirectory;
    }

    @Override
    public File installOrUpdateScanner() throws BlackDuckIntegrationException {
        return existingInstallDirectory;
    }

}
