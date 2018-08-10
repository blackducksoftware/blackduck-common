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
package com.synopsys.integration.hub.cli.summary;

import java.io.File;

import com.synopsys.integration.hub.api.view.ScanSummaryView;
import com.synopsys.integration.hub.summary.Result;

public class ScanTargetOutput {
    private final File dryRunFile;
    private final String errorMessage;
    private final Exception exception;
    private final File logDirectory;
    private final Result result;
    private final ScanSummaryView scanSummaryView;
    private final String scanTarget;

    private ScanTargetOutput(final String errorMessage, final Exception exception, final File logDirectory, final Result result, final File dryRunFile, final ScanSummaryView scanSummaryView, final String scanTarget) {
        this.errorMessage = errorMessage;
        this.exception = exception;
        this.logDirectory = logDirectory;
        this.result = result;
        this.dryRunFile = dryRunFile;
        this.scanSummaryView = scanSummaryView;
        this.scanTarget = scanTarget;
    }

    public static ScanTargetOutput SUCCESS(final String scanTarget, final File logDirectory, final File dryRunFile, final ScanSummaryView scanSummaryView) {
        return new ScanTargetOutput(null, null, logDirectory, Result.SUCCESS, dryRunFile, scanSummaryView, scanTarget);
    }

    public static ScanTargetOutput FAILURE(final String scanTarget, final File logDirectory, final File dryRunFile, final ScanSummaryView scanSummaryView, final String errorMessage,
            final Exception exception) {
        return new ScanTargetOutput(errorMessage, exception, logDirectory, Result.FAILURE, dryRunFile, scanSummaryView, scanTarget);
    }

    public File getLogDirectory() {
        return logDirectory;
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

}
