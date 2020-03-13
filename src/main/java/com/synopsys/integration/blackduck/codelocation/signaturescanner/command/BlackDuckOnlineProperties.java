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

import java.util.List;
import java.util.function.Consumer;

public class BlackDuckOnlineProperties {
    public static final String ONLINE_CAPABILITY_NEEDED_WARNING = "No snippet functionality, license search, or uploading of source is supported when running a dry run signature scan.";

    private final boolean snippetMatching;
    private final boolean snippetMatchingOnly;
    private final boolean fullSnippetScan;
    private final boolean uploadSource;
    private final boolean licenseSearch;

    public BlackDuckOnlineProperties(boolean snippetMatching, boolean snippetMatchingOnly, boolean fullSnippetScan, boolean uploadSource, boolean licenseSearch) {
        this.snippetMatching = snippetMatching;
        this.snippetMatchingOnly = snippetMatchingOnly;
        this.fullSnippetScan = fullSnippetScan;
        this.uploadSource = uploadSource;
        this.licenseSearch = licenseSearch;
    }

    public boolean isOnlineCapabilityNeeded() {
        return snippetMatching || snippetMatchingOnly || uploadSource || licenseSearch;
    }

    public void addOnlineCommands(List<String> cmd) {
        if (snippetMatching || snippetMatchingOnly) {
            if (snippetMatching) {
                cmd.add("--snippet-matching");
            } else {
                cmd.add("--snippet-matching-only");
            }

            if (fullSnippetScan) {
                cmd.add("--full-snippet-scan");
            }
        }

        if (licenseSearch) {
            cmd.add("--license-search");
        }

        if (uploadSource) {
            cmd.add("--upload-source");
        }
    }

    public void warnIfOnlineIsNeeded(Consumer<String> stringConsumer) {
        if (isOnlineCapabilityNeeded()) {
            stringConsumer.accept(ONLINE_CAPABILITY_NEEDED_WARNING);
        }
    }

    public boolean isSnippetMatching() {
        return snippetMatching;
    }

    public boolean isSnippetMatchingOnly() {
        return snippetMatchingOnly;
    }

    public boolean isFullSnippetScan() {
        return fullSnippetScan;
    }

    public boolean isUploadSource() {
        return uploadSource;
    }

    public boolean isLicenseSearch() {
        return licenseSearch;
    }

}
