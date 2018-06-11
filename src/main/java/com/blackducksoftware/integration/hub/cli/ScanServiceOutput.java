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
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class ScanServiceOutput {
    private final File logDirectory;
    private final File cliLogDirectory;
    private final File standardOutputFile;
    private final List<File> dryRunFiles;
    private final List<ScanSummaryView> scanSummaryViews;

    private final ProjectVersionWrapper projectVersionWrapper;

    public ScanServiceOutput(File logDirectory, File cliLogDirectory, File standardOutputFile, List<File> dryRunFiles, List<ScanSummaryView> scanSummaryViews,
            ProjectVersionWrapper projectVersionWrapper) {
        this.logDirectory = logDirectory;
        this.cliLogDirectory = cliLogDirectory;
        this.standardOutputFile = standardOutputFile;
        this.dryRunFiles = dryRunFiles;
        this.scanSummaryViews = scanSummaryViews;
        this.projectVersionWrapper = projectVersionWrapper;
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

    public Optional<List<File>> getDryRunFiles() {
        return Optional.ofNullable(dryRunFiles);
    }

    public Optional<List<ScanSummaryView>> getScanSummaryViews() {
        return Optional.ofNullable(scanSummaryViews);
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper() {
        return Optional.ofNullable(projectVersionWrapper);
    }
}
