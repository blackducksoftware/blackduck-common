/**
 * blackduck-common
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.Set;

public class ScanTarget {
    private final String path;
    private final Set<String> exclusionPatterns;
    private final String codeLocationName;
    private final String outputDirectoryPath;
    private final boolean outputDirectoryPathAbsolute;

    public static ScanTarget createBasicTarget(final String path) {
        return new ScanTarget(path, null, null, null, false);
    }

    public static ScanTarget createBasicTarget(final String path, final String codeLocationName) {
        return new ScanTarget(path, null, codeLocationName, null, false);
    }

    public static ScanTarget createBasicTarget(final String path, final Set<String> exclusionPatterns, final String codeLocationName) {
        return new ScanTarget(path, exclusionPatterns, codeLocationName, null, false);
    }

    private ScanTarget(final String path, final Set<String> exclusionPatterns, final String codeLocationName, final String outputDirectoryPath, final boolean outputDirectoryPathAbsolute) {
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
        return exclusionPatterns;
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

    public static class Builder {
        private String path;
        private Set<String> exclusionPatterns;
        private String codeLocationName;
        private String outputDirectoryPath;
        private boolean outputDirectoryPathAbsolute;

        public Builder(final String path) {
            this.path = path;
        }

        public ScanTarget build() {
            return new ScanTarget(path, exclusionPatterns, codeLocationName, outputDirectoryPath, outputDirectoryPathAbsolute);
        }

        public String getPath() {
            return path;
        }

        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        public Set<String> getExclusionPatterns() {
            return exclusionPatterns;
        }

        public Builder exclusionPatterns(final Set<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
            return this;
        }

        public String getCodeLocationName() {
            return codeLocationName;
        }

        public Builder codeLocationName(final String codeLocationName) {
            this.codeLocationName = codeLocationName;
            return this;
        }

        public String getOutputDirectoryPath() {
            return outputDirectoryPath;
        }

        public Builder outputDirectoryPath(final String outputDirectoryPath) {
            this.outputDirectoryPath = outputDirectoryPath;
            return this;
        }

        public boolean isOutputDirectoryPathAbsolute() {
            return outputDirectoryPathAbsolute;
        }

        public Builder outputDirectoryPathAbsolute(final boolean outputDirectoryPathAbsolute) {
            this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
            return this;
        }

        public Builder outputDirectoryPath(final String outputDirectoryPath, final boolean isAbsolute) {
            this.outputDirectoryPath = outputDirectoryPath;
            outputDirectoryPathAbsolute = isAbsolute;
            return this;
        }
    }
}
