package com.blackducksoftware.integration.hub.policy.api;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

public class PolicyStatus {
    private final String overallStatus;

    private final String updatedAt;

    private final PolicyStatusCounts statusCounts;

    private final PolicyMeta _meta;

    public PolicyStatus(String overallStatus, String updatedAt, PolicyStatusCounts statusCounts, PolicyMeta _meta) {
        this.overallStatus = overallStatus;
        this.updatedAt = updatedAt;
        this.statusCounts = statusCounts;
        this._meta = _meta;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public PolicyStatusEnum getOverallStatusEnum() {
        return PolicyStatusEnum.getPolicyStatusEnum(overallStatus);
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public DateTime getUpdatedAtTime() {
        if (StringUtils.isBlank(updatedAt)) {
            return null;
        }
        try {
            return new DateTime(updatedAt);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public PolicyStatusCounts getStatusCounts() {
        return statusCounts;
    }

    public PolicyMeta get_meta() {
        return _meta;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
        result = prime * result + ((overallStatus == null) ? 0 : overallStatus.hashCode());
        result = prime * result + ((statusCounts == null) ? 0 : statusCounts.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
        if (!(obj instanceof PolicyStatus)) {
            return false;
        }
        PolicyStatus other = (PolicyStatus) obj;
        if (_meta == null) {
            if (other._meta != null) {
                return false;
            }
        } else if (!_meta.equals(other._meta)) {
            return false;
        }
        if (overallStatus == null) {
            if (other.overallStatus != null) {
                return false;
            }
        } else if (!overallStatus.equals(other.overallStatus)) {
            return false;
        }
        if (statusCounts == null) {
            if (other.statusCounts != null) {
                return false;
            }
        } else if (!statusCounts.equals(other.statusCounts)) {
            return false;
        }
        if (updatedAt == null) {
            if (other.updatedAt != null) {
                return false;
            }
        } else if (!updatedAt.equals(other.updatedAt)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PolicyStatus [overallStatus=");
        builder.append(overallStatus);
        builder.append(", updatedAt=");
        builder.append(updatedAt);
        builder.append(", statusCounts=");
        builder.append(statusCounts);
        builder.append(", _meta=");
        builder.append(_meta);
        builder.append("]");
        return builder.toString();
    }

}
