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
package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class ScanServiceOutput {
    private final File cliLogDirectory;
    private final File dryRunFile;
    private final File logDirectory;
    private final ProjectVersionWrapper projectVersionWrapper;
    private final ScanSummaryView scanSummaryView;
    private final String scanTarget;
    private final File standardOutputFile;

    public ScanServiceOutput(String scanTarget, File logDirectory, File cliLogDirectory, File standardOutputFile, File dryRunFile, ScanSummaryView scanSummaryView,
            ProjectVersionWrapper projectVersionWrapper) {
        this.scanTarget = scanTarget;
        this.logDirectory = logDirectory;
        this.cliLogDirectory = cliLogDirectory;
        this.standardOutputFile = standardOutputFile;
        this.dryRunFile = dryRunFile;
        this.scanSummaryView = scanSummaryView;
        this.projectVersionWrapper = projectVersionWrapper;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

    public File getCliLogDirectory() {
        return cliLogDirectory;
    }

    public File getStandardOutputFile() {
        return standardOutputFile;
    }

    public Optional<File> getDryRunFile() {
        return Optional.ofNullable(dryRunFile);
    }

    public Optional<ScanSummaryView> getScanSummaryView() {
        return Optional.ofNullable(scanSummaryView);
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper() {
        return Optional.ofNullable(projectVersionWrapper);
    }
}
