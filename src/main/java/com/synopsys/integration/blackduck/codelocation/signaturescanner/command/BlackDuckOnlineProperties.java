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

    private final SnippetMatching snippetMatchingMode;
    private final boolean uploadSource;
    private final boolean licenseSearch;
    private final boolean copyrightSearch;

    private final boolean snippetMatchingFlag;
    private final boolean snippetMatchingOnlyFlag;
    private final boolean fullSnippetScanFlag;

    public BlackDuckOnlineProperties(SnippetMatching snippetMatchingMode, boolean uploadSource, boolean licenseSearch, boolean copyrightSearch) {
        this.snippetMatchingMode = snippetMatchingMode;
        this.uploadSource = uploadSource;
        this.licenseSearch = licenseSearch;
        this.copyrightSearch = copyrightSearch;

        snippetMatchingFlag = SnippetMatching.SNIPPET_MATCHING == snippetMatchingMode || SnippetMatching.FULL_SNIPPET_MATCHING == snippetMatchingMode;
        snippetMatchingOnlyFlag = SnippetMatching.SNIPPET_MATCHING_ONLY == snippetMatchingMode || SnippetMatching.FULL_SNIPPET_MATCHING_ONLY == snippetMatchingMode;
        fullSnippetScanFlag = SnippetMatching.FULL_SNIPPET_MATCHING == snippetMatchingMode || SnippetMatching.FULL_SNIPPET_MATCHING_ONLY == snippetMatchingMode;
    }

    public boolean isOnlineCapabilityNeeded() {
        return snippetMatchingFlag || snippetMatchingOnlyFlag || uploadSource || licenseSearch;
    }

    public void addOnlineCommands(List<String> cmd) {
        if (snippetMatchingFlag || snippetMatchingOnlyFlag) {
            if (snippetMatchingFlag) {
                cmd.add("--snippet-matching");
            } else {
                cmd.add("--snippet-matching-only");
            }

            if (fullSnippetScanFlag) {
                cmd.add("--full-snippet-scan");
            }
        }

        if (licenseSearch) {
            cmd.add("--license-search");
        }

        if (copyrightSearch) {
            cmd.add("--copyright-search");
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

    public SnippetMatching getSnippetMatchingMode() {
        return snippetMatchingMode;
    }

    public boolean isSnippetMatching() {
        return snippetMatchingFlag;
    }

    public boolean isSnippetMatchingOnly() {
        return snippetMatchingOnlyFlag;
    }

    public boolean isFullSnippetScan() {
        return fullSnippetScanFlag;
    }

    public boolean isUploadSource() {
        return uploadSource;
    }

    public boolean isLicenseSearch() {
        return licenseSearch;
    }

    public boolean isCopyrightSearch() {
        return copyrightSearch;
    }
}
