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
package com.blackducksoftware.integration.hub.configuration;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class HubScanConfigBuilder extends AbstractBuilder<HubScanConfig> {
    private String additionalScanArguments;
    private boolean cleanupLogsOnSuccess;
    private String codeLocationAlias;
    private boolean debug;
    private boolean dryRun;
    private boolean disableScanTargetPathExistenceCheck;
    private boolean enableScanTargetPathsWithinWorkingDirectoryCheck;
    private String[] excludePatterns;
    private String scanMemory;
    private final Set<String> scanTargetPaths = new HashSet<>();
    private boolean snippetModeEnabled;
    private File toolsDir;
    private File workingDirectory;
    private boolean verbose = true;

    @Override
    public HubScanConfig buildObject() {
        final HubScanConfig config = new HubScanConfig(workingDirectory, NumberUtils.toInt(scanMemory), Collections.unmodifiableSet(scanTargetPaths), dryRun, toolsDir, cleanupLogsOnSuccess, excludePatterns,
                codeLocationAlias, debug, verbose, snippetModeEnabled, additionalScanArguments);

        return config;
    }

    @Override
    public AbstractValidator createValidator() {
        final HubScanConfigValidator validator = new HubScanConfigValidator();
        validator.setScanMemory(scanMemory);
        validator.setWorkingDirectory(workingDirectory);
        validator.addAllScanTargetPaths(scanTargetPaths);
        if (disableScanTargetPathExistenceCheck) {
            validator.disableScanTargetPathExistenceCheck();
        }
        if (enableScanTargetPathsWithinWorkingDirectoryCheck) {
            validator.enableScanTargetPathsWithinWorkingDirectoryCheck();
        }
        validator.setExcludePatterns(excludePatterns);
        return validator;
    }

    public void setCleanupLogsOnSuccess(boolean cleanupLogsOnSuccess) {
        this.cleanupLogsOnSuccess = cleanupLogsOnSuccess;
    }

    public void setCodeLocationAlias(final String codeLocationAlias) {
        this.codeLocationAlias = codeLocationAlias;
    }

    public void setToolsDir(final File toolsDir) {
        this.toolsDir = toolsDir;
    }

    public void setDisableScanTargetPathExistenceCheck(final boolean disableScanTargetPathExistenceCheck) {
        this.disableScanTargetPathExistenceCheck = disableScanTargetPathExistenceCheck;
    }

    public void setEnableScanTargetPathsWithinWorkingDirectoryCheck(final boolean enableScanTargetPathsWithinWorkingDirectoryCheck) {
        this.enableScanTargetPathsWithinWorkingDirectoryCheck = enableScanTargetPathsWithinWorkingDirectoryCheck;
    }

    public void setScanMemory(final int scanMemory) {
        setScanMemory(String.valueOf(scanMemory));
    }

    public void setScanMemory(final String scanMemory) {
        this.scanMemory = scanMemory;
    }

    public void addScanTargetPath(final String scanTargetPath) {
        scanTargetPaths.add(scanTargetPath);
    }

    public void addAllScanTargetPaths(final List<String> scanTargetPaths) {
        this.scanTargetPaths.addAll(scanTargetPaths);
    }

    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void disableScanTargetPathExistenceCheck() {
        disableScanTargetPathExistenceCheck = true;
    }

    public void enableScanTargetPathsWithinWorkingDirectoryCheck() {
        enableScanTargetPathsWithinWorkingDirectoryCheck = true;
    }

    public void setExcludePatterns(final String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public void setSnippetModeEnabled(final boolean snippetModeEnabled) {
        this.snippetModeEnabled = snippetModeEnabled;
    }

    public void setAdditionalScanArguments(final String additionalScanArguments) {
        this.additionalScanArguments = additionalScanArguments;
    }

}
