package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

/**
 * Detailed file BOM view entry.
 *
 */
public class DetailedFileBomViewEntry {
    private final UUID projectId;

    private final UUID versionId;

    private final String projectName;

    private final String version;

    private final String matchType;

    private final String matchContent;

    private final String path;

    private final String archiveContext;

    private final String fileBomMatchType;

    private final String usage;

    public DetailedFileBomViewEntry(UUID projectId, UUID versionId, String projectName, String version, String matchType, String matchContent, String path,
            String archiveContext, String fileBomMatchType, String usage) {
        this.projectId = projectId;
        this.versionId = versionId;
        this.projectName = projectName;
        this.version = version;
        this.matchType = matchType;
        this.matchContent = matchContent;
        this.path = path;
        this.archiveContext = archiveContext;
        this.fileBomMatchType = fileBomMatchType;
        this.usage = usage;
    }

    public final UUID getProjectId() {
        return projectId;
    }

    public final UUID getVersionId() {
        return versionId;
    }

    public final String getProjectName() {
        return projectName;
    }

    public final String getVersion() {
        return version;
    }

    public final String getMatchType() {
        return matchType;
    }

    public final String getMatchContent() {
        return matchContent;
    }

    public final String getPath() {
        return path;
    }

    public final String getArchiveContext() {
        return archiveContext;
    }

    public final String getFileBomMatchType() {
        return fileBomMatchType;
    }

    public final String getUsage() {
        return usage;
    }

}
