/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

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

    public BlackDuckOnlineProperties(SnippetMatching snippetMatchingMode, boolean uploadSource, boolean licenseSearch, boolean copyrightSearch) {
        this.snippetMatchingMode = snippetMatchingMode;
        this.uploadSource = uploadSource;
        this.licenseSearch = licenseSearch;
        this.copyrightSearch = copyrightSearch;

        snippetMatchingFlag = SnippetMatching.SNIPPET_MATCHING == snippetMatchingMode;
        snippetMatchingOnlyFlag = SnippetMatching.SNIPPET_MATCHING_ONLY == snippetMatchingMode;
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
