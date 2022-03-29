package com.synopsys.integration.blackduck.bdio2.model;

import java.net.URL;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public class GitInfo {
    @Nullable
    private final URL sourceRepository;
    @Nullable
    private final String sourceRevision;
    @Nullable
    private final String sourceBranch;

    public static GitInfo none() {
        return new GitInfo(null, null, null);
    }

    public GitInfo(@Nullable URL sourceRepository, @Nullable String sourceRevision, @Nullable String sourceBranch) {
        this.sourceRepository = sourceRepository;
        this.sourceRevision = sourceRevision;
        this.sourceBranch = sourceBranch;
    }

    public Optional<URL> getSourceRepository() {
        return Optional.ofNullable(sourceRepository);
    }

    public Optional<String> getSourceRevision() {
        return Optional.ofNullable(sourceRevision);
    }

    public Optional<String> getSourceBranch() {
        return Optional.ofNullable(sourceBranch);
    }
}
