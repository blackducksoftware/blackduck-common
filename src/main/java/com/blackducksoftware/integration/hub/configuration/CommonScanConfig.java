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

public class CommonScanConfig {
    private final String additionalScanArguments;
    private final boolean debug;
    private final boolean dryRun;
    private final int scanMemory;
    private final boolean snippetModeEnabled;
    private final File toolsDir;
    private final File workingDirectory;
    private final boolean verbose;

    public CommonScanConfig(final String additionalScanArguments, final boolean debug, final boolean dryRun, final int scanMemory, final boolean snippetModeEnabled, final File toolsDir, final File workingDirectory, final boolean verbose) {
        this.additionalScanArguments = additionalScanArguments;
        this.debug = debug;
        this.dryRun = dryRun;
        this.scanMemory = scanMemory;
        this.snippetModeEnabled = snippetModeEnabled;
        this.toolsDir = toolsDir;
        this.workingDirectory = workingDirectory;
        this.verbose = verbose;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public int getScanMemory() {
        return scanMemory;
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
