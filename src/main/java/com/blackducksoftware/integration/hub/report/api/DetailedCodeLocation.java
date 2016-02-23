package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * Detailed code location.
 *
 */
public class DetailedCodeLocation {
    private final UUID codeLocationId;

    private final String host;

    private final String path;

    private final String initiatedBy;

    private final String lastUpload;

    public DetailedCodeLocation(UUID codeLocationId,
            String host,
            String path,
            String initiatedBy,
            String lastUpload) {
        this.codeLocationId = codeLocationId;
        this.host = host;
        this.path = path;
        this.initiatedBy = initiatedBy;
        this.lastUpload = lastUpload;
    }

    public UUID getCodeLocationId() {
        return codeLocationId;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public String getLastUpload() {
        return lastUpload;
    }

    public DateTime getLastUploadTime() {
        if (StringUtils.isBlank(lastUpload)) {
            return null;
        }
        return new DateTime(lastUpload);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codeLocationId == null) ? 0 : codeLocationId.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((initiatedBy == null) ? 0 : initiatedBy.hashCode());
        result = prime * result + ((lastUpload == null) ? 0 : lastUpload.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        DetailedCodeLocation other = (DetailedCodeLocation) obj;
        if (codeLocationId == null) {
            if (other.codeLocationId != null) {
                return false;
            }
        } else if (!codeLocationId.equals(other.codeLocationId)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (initiatedBy == null) {
            if (other.initiatedBy != null) {
                return false;
            }
        } else if (!initiatedBy.equals(other.initiatedBy)) {
            return false;
        }
        if (lastUpload == null) {
            if (other.lastUpload != null) {
                return false;
            }
        } else if (!lastUpload.equals(other.lastUpload)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DetailedCodeLocation [codeLocationId=");
        builder.append(codeLocationId);
        builder.append(", host=");
        builder.append(host);
        builder.append(", path=");
        builder.append(path);
        builder.append(", initiatedBy=");
        builder.append(initiatedBy);
        builder.append(", lastUpload=");
        builder.append(lastUpload);
        builder.append("]");
        return builder.toString();
    }

}
