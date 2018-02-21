package com.blackducksoftware.integration.hub.service;

import com.blackducksoftware.integration.hub.api.core.HubResponse;

public class DryRunUploadResponse extends HubResponse {
    public String codeLocationId;
    public String scannerVersion;
    public String signatureVersion;
    public String id;
    public String scanType;
    public String name;
    public String hostName;
    public String baseDir;
    public String ownerEntityKeyToken;
    public String lastModifiedOn;
    public String createdOn;
    public String timeToScan;
    public String createdByUserId;
    public String status;
    public String fileSystemSize;
    public String matchCount;
    public String numDirs;
    public String numNonDirFiles;
    public String scanSourceType;
    public String scanSourceId;
    public String scanTime;
    public String timeLastModified;
    public String timeToPersistMs;
}
