package com.synopsys.integration.blackduck.bdio2.model;

import java.net.URL;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.util.NameVersion;

// project name, project version name, project group, correlation id, and git info.
public class ProjectInfo {
    private final NameVersion nameVersion;
    @Nullable
    private final String projectGroup;
    @Nullable
    private final String correlationId;
    @Nullable
    private final URL sourceRepository;
    @Nullable
    private final String sourceRevision;
    @Nullable
    private final String sourceBranch;

    public static ProjectInfo simple(NameVersion nameVersion) {
        return new ProjectInfo(nameVersion, null, null, null, null, null);
    }

    public ProjectInfo(
        NameVersion nameVersion,
        @Nullable String projectGroup,
        @Nullable String correlationId,
        @Nullable URL sourceRepository,
        @Nullable String sourceRevision,
        @Nullable String sourceBranch
    ) {
        this.nameVersion = nameVersion;
        this.projectGroup = projectGroup;
        this.correlationId = correlationId;
        this.sourceRepository = sourceRepository;
        this.sourceRevision = sourceRevision;
        this.sourceBranch = sourceBranch;
    }

    public NameVersion getNameVersion() {
        return nameVersion;
    }

    public Optional<String> getProjectGroup() {
        return Optional.ofNullable(projectGroup);
    }

    public Optional<String> getCorrelationId() {
        return Optional.ofNullable(correlationId);
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
