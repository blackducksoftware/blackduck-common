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
package com.synopsys.integration.blackduck.cli.simple;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.log.IntLogger;

public class SimpleScanResult {
    private static final FilenameFilter JSON_FILTER = (dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json");

    private final IntLogger logger;
    private final File specificRunOutputDirectory;

    public SimpleScanResult(final IntLogger logger, final File specificRunOutputDirectory) {
        this.logger = logger;
        this.specificRunOutputDirectory = specificRunOutputDirectory;
    }

    public File getLogDirectory() {
        return specificRunOutputDirectory;
    }

    public File getStatusDirectory() {
        return new File(specificRunOutputDirectory, "status");
    }

    public File getDataDirectory() {
        return new File(specificRunOutputDirectory, "data");
    }

    public File getCLILogDirectory() {
        return new File(specificRunOutputDirectory, "log");
    }

    public File getStandardOutputFile() {
        return new File(specificRunOutputDirectory, "CLI_Output.txt");
    }

    public File getScanSummaryFile() {
        final File scanStatusDirectory = getStatusDirectory();
        if (null != scanStatusDirectory) {
            final File[] scanSummaryFiles = scanStatusDirectory.listFiles(JSON_FILTER);
            if (null != scanSummaryFiles) {
                if (scanSummaryFiles.length == 0) {
                    logger.error("There were no status files found in " + scanStatusDirectory.getAbsolutePath());
                    return null;
                } else if (scanSummaryFiles.length > 1) {
                    logger.error(String.format("There were should have only been 1 status file in '%s' but there are %s", scanStatusDirectory.getAbsolutePath(), scanSummaryFiles.length));
                }
                return scanSummaryFiles[0];
            }
        }
        return null;
    }

    public File getDryRunFile() {
        final File dataDirectory = getDataDirectory();
        if (null != dataDirectory) {
            final File[] dryRunFiles = dataDirectory.listFiles(JSON_FILTER);
            if (null != dryRunFiles) {
                if (dryRunFiles.length == 0) {
                    logger.error("There were no dry run files found in " + dataDirectory.getAbsolutePath());
                    return null;
                } else if (dryRunFiles.length > 1) {
                    logger.error(String.format("There were should have only been 1 dry run in '%s' but there are %s", dataDirectory.getAbsolutePath(), dryRunFiles.length));
                }
                return dryRunFiles[0];
            }
        }
        return null;
    }

}
