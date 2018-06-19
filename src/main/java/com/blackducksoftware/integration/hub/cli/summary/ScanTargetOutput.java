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
package com.blackducksoftware.integration.hub.cli.summary;

import java.io.File;

import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.summary.Result;

public class ScanTargetOutput {
    private final File cliLogDirectory;
    private final String errorMessage;
    private final Exception exception;
    private final Result result;
    private final File dryRunFile;
    private final ScanSummaryView scanSummaryView;
    private final String scanTarget;
    private final File standardOutputFile;

    private ScanTargetOutput(final File cliLogDirectory, final String errorMessage, final Exception exception, final Result result, final File dryRunFile, final ScanSummaryView scanSummaryView, final String scanTarget,
            final File standardOutputFile) {
        this.cliLogDirectory = cliLogDirectory;
        this.errorMessage = errorMessage;
        this.exception = exception;
        this.result = result;
        this.dryRunFile = dryRunFile;
        this.scanSummaryView = scanSummaryView;
        this.scanTarget = scanTarget;
        this.standardOutputFile = standardOutputFile;
    }

    public static ScanTargetOutput SUCCESS(final String scanTarget, final File cliLogDirectory, final File standardOutputFile, final File dryRunFile, final ScanSummaryView scanSummaryView) {
        return new ScanTargetOutput(cliLogDirectory, null, null, Result.SUCCESS, dryRunFile, scanSummaryView, scanTarget, standardOutputFile);
    }

    public static ScanTargetOutput FAILURE(final String scanTarget, final File cliLogDirectory, final File standardOutputFile, final File dryRunFile, final ScanSummaryView scanSummaryView, final String errorMessage,
            final Exception exception) {
        return new ScanTargetOutput(cliLogDirectory, errorMessage, exception, Result.FAILURE, dryRunFile, scanSummaryView, scanTarget, standardOutputFile);
    }

    public File getCliLogDirectory() {
        return cliLogDirectory;
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

    public File getDryRunFile() {
        return dryRunFile;
    }

    public ScanSummaryView getScanSummaryView() {
        return scanSummaryView;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public File getStandardOutputFile() {
        return standardOutputFile;
    }
}
