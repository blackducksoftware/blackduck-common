package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

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

    private final DateTime lastUpload;

    public DetailedCodeLocation(UUID codeLocationId,
            String host,
            String path,
            String initiatedBy,
            DateTime lastUpload) {
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

    public DateTime getLastUpload() {
        return lastUpload;
    }
}
