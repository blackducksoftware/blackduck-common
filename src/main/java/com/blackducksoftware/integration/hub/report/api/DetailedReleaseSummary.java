package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Detailed release summary.
 *
 * @author skatzman
 */
public class DetailedReleaseSummary {
    private final UUID projectId;

    private final UUID versionId;

    private final String projectName;

    private final String version;

    private final String versionComments;

    private final String nickname;

    private final DateTime releasedOn;

    private final String phase;

    private final String distribution;

    public DetailedReleaseSummary(UUID projectId,
            UUID versionId,
            String projectName,
            String version,
            String versionComments,
            String nickname,
            DateTime releasedOn,
            String phase,
            String distribution) {
        this.projectId = projectId;
        this.versionId = versionId;
        this.projectName = projectName;
        this.version = version;
        this.versionComments = versionComments;
        this.nickname = nickname;
        this.releasedOn = releasedOn;
        this.phase = phase;
        this.distribution = distribution;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionComments() {
        return versionComments;
    }

    public String getNickname() {
        return nickname;
    }

    public DateTime getReleasedOn() {
        return releasedOn;
    }

    public String getPhase() {
        return phase;
    }

    public String getDistribution() {
        return distribution;
    }

}
