/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;

import java.io.File;

public interface ScannerInstaller {
    File installOrUpdateScanner() throws BlackDuckIntegrationException;

}
