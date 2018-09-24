/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.blackduck.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.HubIntegrationException;

public class OutputDirectoryCallable implements Callable<File> {
    private final ScanPathsUtility scanPathsUtility;
    private final File outputDirectory;
    private final ScanTarget scanTarget;

    public OutputDirectoryCallable(final ScanPathsUtility scanPathsUtility, final File outputDirectory, final ScanTarget scanTarget) {
        this.scanPathsUtility = scanPathsUtility;
        this.outputDirectory = outputDirectory;
        this.scanTarget = scanTarget;
    }

    @Override
    public File call() throws IOException, HubIntegrationException {
        File commandOutputDirectory = null;
        if (StringUtils.isNotBlank(scanTarget.getOutputDirectoryPath())) {
            if (scanTarget.isOutputDirectoryPathAbsolute()) {
                commandOutputDirectory = new File(scanTarget.getOutputDirectoryPath());
            } else {
                commandOutputDirectory = new File(outputDirectory, scanTarget.getOutputDirectoryPath());
            }
            commandOutputDirectory.mkdirs();
        } else {
            commandOutputDirectory = scanPathsUtility.createSpecificRunOutputDirectory(outputDirectory);
        }

        return commandOutputDirectory;
    }

}
