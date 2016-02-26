package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * Detailed release summary.
 *
 */
public class DetailedReleaseSummary {
    private final String projectId;

    private final String versionId;

    private final String projectName;

    private final String version;

    private final String versionComments;

    private final String nickname;

    private final String releasedOn;

    private final String phase;

    private final String distribution;

    private final URLProvider uiUrlGenerator;

    public DetailedReleaseSummary(String projectId,
            String versionId,
            String projectName,
            String version,
            String versionComments,
            String nickname,
            String releasedOn,
            String phase,
            String distribution, URLProvider uiUrlGenerator) {
        this.projectId = projectId;
        this.versionId = versionId;
        this.projectName = projectName;
        this.version = version;
        this.versionComments = versionComments;
        this.nickname = nickname;
        this.releasedOn = releasedOn;
        this.phase = phase;
        this.distribution = distribution;
        this.uiUrlGenerator = uiUrlGenerator;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public UUID getProjectUUId() {
        return UUID.fromString(projectId);
    }

    public UUID getVersionUUId() {
        return UUID.fromString(versionId);
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

    public String getReleasedOn() {
        return releasedOn;
    }

    public String getPhase() {
        return phase;
    }

    public String getDistribution() {
        return distribution;
    }

    public DateTime getReleasedOnTime() {
        if (StringUtils.isBlank(releasedOn)) {
            return null;
        }
        return new DateTime(releasedOn);
    }

    public URLProvider getUiUrlGenerator() {
        return uiUrlGenerator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
        result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((releasedOn == null) ? 0 : releasedOn.hashCode());
        result = prime * result + ((uiUrlGenerator == null) ? 0 : uiUrlGenerator.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((versionComments == null) ? 0 : versionComments.hashCode());
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
        DetailedReleaseSummary other = (DetailedReleaseSummary) obj;
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        } else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (nickname == null) {
            if (other.nickname != null) {
                return false;
            }
        } else if (!nickname.equals(other.nickname)) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
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
        if (releasedOn == null) {
            if (other.releasedOn != null) {
                return false;
            }
        } else if (!releasedOn.equals(other.releasedOn)) {
            return false;
        }
        if (uiUrlGenerator == null) {
            if (other.uiUrlGenerator != null) {
                return false;
            }
        } else if (!uiUrlGenerator.equals(other.uiUrlGenerator)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (versionComments == null) {
            if (other.versionComments != null) {
                return false;
            }
        } else if (!versionComments.equals(other.versionComments)) {
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
        builder.append("DetailedReleaseSummary [projectId=");
        builder.append(projectId);
        builder.append(", versionId=");
        builder.append(versionId);
        builder.append(", projectName=");
        builder.append(projectName);
        builder.append(", version=");
        builder.append(version);
        builder.append(", versionComments=");
        builder.append(versionComments);
        builder.append(", nickname=");
        builder.append(nickname);
        builder.append(", releasedOn=");
        builder.append(releasedOn);
        builder.append(", phase=");
        builder.append(phase);
        builder.append(", distribution=");
        builder.append(distribution);
        builder.append(", uiUrlGenerator=");
        builder.append(uiUrlGenerator);
        builder.append("]");
        return builder.toString();
    }

    public class URLProvider {
        private final String baseUrl;

        public URLProvider(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
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
            URLProvider other = (URLProvider) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (baseUrl == null) {
                if (other.baseUrl != null) {
                    return false;
                }
            } else if (!baseUrl.equals(other.baseUrl)) {
                return false;
            }
            return true;
        }

        private DetailedReleaseSummary getOuterType() {
            return DetailedReleaseSummary.this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("URLProvider [baseUrl=");
            builder.append(baseUrl);
            builder.append("]");
            return builder.toString();
        }

    }

}
