/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.log.IntLogger;

public class ScanCommandOutput extends CodeLocationOutput {
    public static final String DRY_RUN_RESULT_DIRECTORY = "data";
    public static final String SCAN_RESULT_DIRECTORY = "status";

    private final IntLogger logger;
    private final ScanCommand scanCommand;
    private final String executedScanCommand;
    private final Integer scanExitCode;

    public static ScanCommandOutput SUCCESS(String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand) {
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(codeLocationName, expectedNotificationCount, Result.SUCCESS, logger, scanCommand, executedScanCommand, null, null, 0);
    }

    public static ScanCommandOutput FAILURE(String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, String errorMessage, Exception exception) {
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(codeLocationName, expectedNotificationCount, Result.FAILURE, logger, scanCommand, executedScanCommand, errorMessage, exception, null);
    }

    public static ScanCommandOutput FAILURE(String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, int scanExitCode) {
        String errorMessage = String.format("The scan failed with return code: %d", scanExitCode);
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(codeLocationName, expectedNotificationCount, Result.FAILURE, logger, scanCommand, executedScanCommand, errorMessage, null, Integer.valueOf(scanExitCode));
    }

    private static int calculateExpectedNotificationCount(ScanCommand scanCommand) {
        if (scanCommand.isSnippetMatching() || scanCommand.isSnippetMatchingOnly()) {
            return 2;
        } else {
            return 1;
        }
    }

    private ScanCommandOutput(String codeLocationName, int expectedNotificationCount, Result result, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, String errorMessage, Exception exception, Integer scanExitCode) {
        super(result, codeLocationName, expectedNotificationCount, errorMessage, exception);
        this.logger = logger;
        this.scanCommand = scanCommand;
        this.executedScanCommand = executedScanCommand;
        this.scanExitCode = scanExitCode;
    }

    private Optional<File> getResultFile(String resultDirectoryName) {
        File resultDirectory = new File(scanCommand.getOutputDirectory(), resultDirectoryName);
        if (null != resultDirectory && resultDirectory.exists()) {
            File[] resultFiles = resultDirectory.listFiles((dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json"));
            if (null != resultFiles && resultFiles.length == 1) {
                return Optional.of(resultFiles[0]);
            }
        }
        logger.error(String.format("Exactly 1 result file was not found in the result directory: %s", resultDirectory.getAbsolutePath()));
        return Optional.empty();
    }

    public boolean wasDryRun() {
        return scanCommand.isDryRun();
    }

    public Optional<File> getScanSummaryFile() {
        return getResultFile(SCAN_RESULT_DIRECTORY);
    }

    public Optional<File> getDryRunFile() {
        return getResultFile(DRY_RUN_RESULT_DIRECTORY);
    }

    public File getSpecificRunOutputDirectory() {
        return scanCommand.getOutputDirectory();
    }

    public String getScanTarget() {
        return scanCommand.getTargetPath();
    }

    public String getExecutedScanCommand() {
        return executedScanCommand;
    }

    public Optional<Integer> getScanExitCode() {
        return Optional.ofNullable(scanExitCode);
    }

}
