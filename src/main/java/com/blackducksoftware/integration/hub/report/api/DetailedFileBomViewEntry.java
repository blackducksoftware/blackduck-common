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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((archiveContext == null) ? 0 : archiveContext.hashCode());
        result = prime * result + ((fileBomMatchType == null) ? 0 : fileBomMatchType.hashCode());
        result = prime * result + ((matchContent == null) ? 0 : matchContent.hashCode());
        result = prime * result + ((matchType == null) ? 0 : matchType.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((usage == null) ? 0 : usage.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DetailedFileBomViewEntry other = (DetailedFileBomViewEntry) obj;
        if (archiveContext == null) {
            if (other.archiveContext != null) {
                return false;
            }
        } else if (!archiveContext.equals(other.archiveContext)) {
            return false;
        }
        if (fileBomMatchType == null) {
            if (other.fileBomMatchType != null) {
                return false;
            }
        } else if (!fileBomMatchType.equals(other.fileBomMatchType)) {
            return false;
        }
        if (matchContent == null) {
            if (other.matchContent != null) {
                return false;
            }
        } else if (!matchContent.equals(other.matchContent)) {
            return false;
        }
        if (matchType == null) {
            if (other.matchType != null) {
                return false;
            }
        } else if (!matchType.equals(other.matchType)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (projectId == null) {
            if (other.projectId != null) {
                return false;
            }
        } else if (!projectId.equals(other.projectId)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        if (usage == null) {
            if (other.usage != null) {
                return false;
            }
        } else if (!usage.equals(other.usage)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (versionId == null) {
            if (other.versionId != null) {
                return false;
            }
        } else if (!versionId.equals(other.versionId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DetailedFileBomViewEntry [projectId=");
        builder.append(projectId);
        builder.append(", versionId=");
        builder.append(versionId);
        builder.append(", projectName=");
        builder.append(projectName);
        builder.append(", version=");
        builder.append(version);
        builder.append(", matchType=");
        builder.append(matchType);
        builder.append(", matchContent=");
        builder.append(matchContent);
        builder.append(", path=");
        builder.append(path);
        builder.append(", archiveContext=");
        builder.append(archiveContext);
        builder.append(", fileBomMatchType=");
        builder.append(fileBomMatchType);
        builder.append(", usage=");
        builder.append(usage);
        builder.append("]");
        return builder.toString();
    }

}
