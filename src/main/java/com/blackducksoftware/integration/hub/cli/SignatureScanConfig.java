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

public class SignatureScanConfig {
    private final String additionalScanArguments;
    private final String codeLocationAlias;
    private final boolean debug;
    private final boolean dryRun;
    private final String[] excludePatterns;
    private final int scanMemory;
    private final String scanTarget;
    private final boolean snippetModeEnabled;
    private final File toolsDir;
    private final File workingDirectory;
    private final boolean verbose;

    public SignatureScanConfig(String additionalScanArguments, String codeLocationAlias, boolean debug, boolean dryRun, String[] excludePatterns, int scanMemory, String scanTarget,
            boolean snippetModeEnabled, File toolsDir, File workingDirectory, boolean verbose) {
        this.additionalScanArguments = additionalScanArguments;
        this.codeLocationAlias = codeLocationAlias;
        this.debug = debug;
        this.dryRun = dryRun;
        this.excludePatterns = excludePatterns;
        this.scanMemory = scanMemory;
        this.scanTarget = scanTarget;
        this.snippetModeEnabled = snippetModeEnabled;
        this.toolsDir = toolsDir;
        this.workingDirectory = workingDirectory;
        this.verbose = verbose;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public String getCodeLocationAlias() {
        return codeLocationAlias;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public boolean isSnippetModeEnabled() {
        return snippetModeEnabled;
    }

    public File getToolsDir() {
        return toolsDir;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
