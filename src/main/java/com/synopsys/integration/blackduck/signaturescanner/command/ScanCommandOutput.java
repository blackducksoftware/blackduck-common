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
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.blackduck.summary.Result;
import com.synopsys.integration.log.IntLogger;

public class ScanCommandOutput {
    public static final String DRY_RUN_RESULT_DIRECTORY = "data";
    public static final String SCAN_RESULT_DIRECTORY = "status";

    private final IntLogger logger;
    private final Result result;
    private final String errorMessage;
    private final Exception exception;
    private final File specificRunOutputDirectory;
    private final String targetPath;
    private final boolean dryRun;

    public static ScanCommandOutput SUCCESS(final IntLogger logger, final File specificRunOutputDirectory, final String targetPath, final boolean dryRun) {
        return new ScanCommandOutput(logger, specificRunOutputDirectory, targetPath, dryRun, Result.SUCCESS, null, null);
    }

    public static ScanCommandOutput FAILURE(final IntLogger logger, final File specificRunOutputDirectory, final String targetPath, final boolean dryRun, final String errorMessage, final Exception exception) {
        return new ScanCommandOutput(logger, specificRunOutputDirectory, targetPath, dryRun, Result.FAILURE, errorMessage, exception);
    }

    private ScanCommandOutput(final IntLogger logger, final File specificRunOutputDirectory, final String targetPath, final boolean dryRun, final Result result, final String errorMessage, final Exception exception) {
        this.logger = logger;
        this.result = result;
        this.errorMessage = errorMessage;
        this.exception = exception;
        this.specificRunOutputDirectory = specificRunOutputDirectory;
        this.targetPath = targetPath;
        this.dryRun = dryRun;
    }

    private Optional<File> getResultFile(final String resultDirectoryName) {
        final File resultDirectory = new File(specificRunOutputDirectory, resultDirectoryName);
        if (null != resultDirectory && resultDirectory.exists()) {
            final File[] resultFiles = resultDirectory.listFiles((dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json"));
            if (null != resultFiles && resultFiles.length == 1) {
                return Optional.of(resultFiles[0]);
            }
        }
        logger.error(String.format("Exactly 1 result file was not found in the result directory: %s", resultDirectory.getAbsolutePath()));
        return Optional.empty();
    }

    public boolean wasDryRun() {
        return dryRun;
    }

    public Optional<File> getScanSummaryFile() {
        return getResultFile(SCAN_RESULT_DIRECTORY);
    }

    public Optional<File> getDryRunFile() {
        return getResultFile(DRY_RUN_RESULT_DIRECTORY);
    }

    public File getSpecificRunOutputDirectory() {
        return specificRunOutputDirectory;
    }

    public Result getResult() {
        return result;
    }

    public String getScanTarget() {
        return targetPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Exception getException() {
        return exception;
    }

}
