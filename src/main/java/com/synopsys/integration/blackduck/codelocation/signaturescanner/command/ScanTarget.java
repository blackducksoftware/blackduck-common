/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class ScanTarget {
    private final String path;
    private final Set<String> exclusionPatterns;
    private final String codeLocationName;
    private final String outputDirectoryPath;
    private final boolean outputDirectoryPathAbsolute;

    public static ScanTarget createBasicTarget(String path) {
        return new ScanTarget(path, null, null, null, false);
    }

    public static ScanTarget createBasicTarget(String path, String codeLocationName) {
        return new ScanTarget(path, null, codeLocationName, null, false);
    }

    public static ScanTarget createBasicTarget(String path, Set<String> exclusionPatterns, String codeLocationName) {
        return new ScanTarget(path, exclusionPatterns, codeLocationName, null, false);
    }

    private ScanTarget(String path, Set<String> exclusionPatterns, String codeLocationName, String outputDirectoryPath, boolean outputDirectoryPathAbsolute) {
        this.path = path;
        this.exclusionPatterns = exclusionPatterns;
        this.codeLocationName = codeLocationName;
        this.outputDirectoryPath = outputDirectoryPath;
        this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getExclusionPatterns() {
        return Optional.ofNullable(exclusionPatterns).orElse(Collections.emptySet())
                   .stream()
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public String getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public boolean isOutputDirectoryPathAbsolute() {
        return outputDirectoryPathAbsolute;
    }

    public File determineCommandOutputDirectory(ScanPathsUtility scanPathsUtility, File outputDirectory) throws BlackDuckIntegrationException {
        if (StringUtils.isNotBlank(getOutputDirectoryPath())) {
            File commandOutputDirectory;
            if (isOutputDirectoryPathAbsolute()) {
                commandOutputDirectory = new File(getOutputDirectoryPath());
            } else {
                commandOutputDirectory = new File(outputDirectory, getOutputDirectoryPath());
            }
            commandOutputDirectory.mkdirs();
            return commandOutputDirectory;
        } else {
            return scanPathsUtility.createSpecificRunOutputDirectory(outputDirectory);
        }
    }

    public static class Builder {
        private String path;
        private Set<String> exclusionPatterns;
        private String codeLocationName;
        private String outputDirectoryPath;
        private boolean outputDirectoryPathAbsolute;

        public Builder(String path) {
            this.path = path;
        }

        public ScanTarget build() {
            return new ScanTarget(path, exclusionPatterns, codeLocationName, outputDirectoryPath, outputDirectoryPathAbsolute);
        }

        public String getPath() {
            return path;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Set<String> getExclusionPatterns() {
            return exclusionPatterns;
        }

        public Builder exclusionPatterns(Set<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
            return this;
        }

        public String getCodeLocationName() {
            return codeLocationName;
        }

        public Builder codeLocationName(String codeLocationName) {
            this.codeLocationName = codeLocationName;
            return this;
        }

        public String getOutputDirectoryPath() {
            return outputDirectoryPath;
        }

        public Builder outputDirectoryPath(String outputDirectoryPath) {
            this.outputDirectoryPath = outputDirectoryPath;
            return this;
        }

        public boolean isOutputDirectoryPathAbsolute() {
            return outputDirectoryPathAbsolute;
        }

        public Builder outputDirectoryPathAbsolute(boolean outputDirectoryPathAbsolute) {
            this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
            return this;
        }

        public Builder outputDirectoryPath(String outputDirectoryPath, boolean isAbsolute) {
            this.outputDirectoryPath = outputDirectoryPath;
            outputDirectoryPathAbsolute = isAbsolute;
            return this;
        }
    }
}
