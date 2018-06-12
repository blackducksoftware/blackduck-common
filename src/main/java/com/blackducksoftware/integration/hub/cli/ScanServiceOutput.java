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

import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.summary.Result;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class ScanServiceOutput {
    private final File cliLogDirectory;
    private final File dryRunFile;
    private final String errorMessage;
    private final Exception exception;
    private final File logDirectory;
    private final ProjectVersionWrapper projectVersionWrapper;
    private final Result result;
    private final ScanSummaryView scanSummaryView;
    private final String scanTarget;
    private final File standardOutputFile;

    private ScanServiceOutput(String scanTarget, File logDirectory, File cliLogDirectory, File standardOutputFile, File dryRunFile, ScanSummaryView scanSummaryView, ProjectVersionWrapper projectVersionWrapper, Result result,
            String errorMessage, Exception exception) {
        this.cliLogDirectory = cliLogDirectory;
        this.dryRunFile = dryRunFile;
        this.logDirectory = logDirectory;
        this.projectVersionWrapper = projectVersionWrapper;
        this.result = result;
        this.scanSummaryView = scanSummaryView;
        this.scanTarget = scanTarget;
        this.standardOutputFile = standardOutputFile;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public static ScanServiceOutput SUCCESS(String scanTarget, File logDirectory, File cliLogDirectory, File standardOutputFile, File dryRunFile, ScanSummaryView scanSummaryView, ProjectVersionWrapper projectVersionWrapper) {
        return new ScanServiceOutput(scanTarget, logDirectory, cliLogDirectory, standardOutputFile, dryRunFile, scanSummaryView, projectVersionWrapper, Result.SUCCESS, null, null);
    }

    public static ScanServiceOutput FAILURE(String scanTarget, File logDirectory, File cliLogDirectory, File standardOutputFile, File dryRunFile, ScanSummaryView scanSummaryView, ProjectVersionWrapper projectVersionWrapper,
            String errorMessage, Exception e) {
        return new ScanServiceOutput(scanTarget, logDirectory, cliLogDirectory, standardOutputFile, dryRunFile, scanSummaryView, projectVersionWrapper, Result.SUCCESS, errorMessage, e);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Exception getException() {
        return exception;
    }

    public Result getResult() {
        return result;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public File getCliLogDirectory() {
        return cliLogDirectory;
    }

    public File getDryRunFile() {
        return dryRunFile;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

    public ProjectVersionWrapper getProjectVersionWrapper() {
        return projectVersionWrapper;
    }

    public ScanSummaryView getScanSummaryView() {
        return scanSummaryView;
    }

    public File getStandardOutputFile() {
        return standardOutputFile;
    }
}
