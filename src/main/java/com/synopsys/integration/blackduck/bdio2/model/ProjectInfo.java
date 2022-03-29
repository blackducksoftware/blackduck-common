package com.synopsys.integration.blackduck.bdio2.model;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.util.NameVersion;

// Additional fields for the bdio-header.jsonld file
public class ProjectInfo {
    private final NameVersion nameVersion;
    @Nullable
    private final String projectGroup;
    @Nullable
    private final String correlationId;
    private final GitInfo gitInfo;

    public static ProjectInfo nameVersion(NameVersion nameVersion) {
        return ProjectInfo.nameVersionGit(nameVersion, GitInfo.none());
    }

    public static ProjectInfo nameVersionGroup(NameVersion nameVersion, String group) {
        return ProjectInfo.nameVersionGroupGit(nameVersion, group, GitInfo.none());
    }

    public static ProjectInfo nameVersionGit(NameVersion nameVersion, GitInfo gitInfo) {
        return ProjectInfo.nameVersionGroupGit(nameVersion, null, gitInfo);
    }

    public static ProjectInfo nameVersionGroupGit(NameVersion nameVersion, String group, GitInfo gitInfo) {
        return new ProjectInfo(nameVersion, group, null, gitInfo);
    }

    public ProjectInfo(
        NameVersion nameVersion,
        @Nullable String projectGroup,
        @Nullable String correlationId,
        GitInfo gitInfo
    ) {
        this.nameVersion = nameVersion;
        this.projectGroup = projectGroup;
        this.correlationId = correlationId;
        this.gitInfo = gitInfo;
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

    public GitInfo getGitInfo() {
        return gitInfo;
    }
}
