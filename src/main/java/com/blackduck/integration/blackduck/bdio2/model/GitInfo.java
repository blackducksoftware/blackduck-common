/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2.model;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GitInfo {
    @Nullable
    private final String sourceRepository;
    @Nullable
    private final String sourceRevision;
    @Nullable
    private final String sourceBranch;

    public static GitInfo none() {
        return new GitInfo(null, null, null);
    }

    public GitInfo(@Nullable String sourceRepository, @Nullable String sourceRevision, @Nullable String sourceBranch) {
        this.sourceRepository = sourceRepository;
        this.sourceRevision = sourceRevision;
        this.sourceBranch = sourceBranch;
    }

    public Optional<String> getSourceRepository() {
        return Optional.ofNullable(sourceRepository);
    }

    public Optional<String> getSourceRevision() {
        return Optional.ofNullable(sourceRevision);
    }

    public Optional<String> getSourceBranch() {
        return Optional.ofNullable(sourceBranch);
    }
}
