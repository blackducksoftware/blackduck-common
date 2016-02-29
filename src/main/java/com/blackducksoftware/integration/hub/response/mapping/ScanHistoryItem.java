package com.blackducksoftware.integration.hub.response.mapping;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

public class ScanHistoryItem {

    private String scannerVersion;

    private String lastModifiedOn;

    private String createdOn;

    private String createdByUserName;

    private ScanStatus status;

    private String scanSourceType;

    private String numDirs;

    private String numNonDirFiles;

    public String getScannerVersion() {
        return scannerVersion;
    }

    public void setScannerVersion(String scannerVersion) {
        this.scannerVersion = scannerVersion;
    }

    public String getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(String lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public String getScanSourceType() {
        return scanSourceType;
    }

    public void setScanSourceType(String scanSourceType) {
        this.scanSourceType = scanSourceType;
    }

    public String getNumDirs() {
        return numDirs;
    }

    public void setNumDirs(String numDirs) {
        this.numDirs = numDirs;
    }

    public String getNumNonDirFiles() {
        return numNonDirFiles;
    }

    public void setNumNonDirFiles(String numNonDirFiles) {
        this.numNonDirFiles = numNonDirFiles;
    }

    public DateTime getCreatedOnTime() {
        if (StringUtils.isBlank(createdOn)) {
            return null;
        }
        return new DateTime(createdOn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdByUserName == null) ? 0 : createdByUserName.hashCode());
        result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((lastModifiedOn == null) ? 0 : lastModifiedOn.hashCode());
        result = prime * result + ((numDirs == null) ? 0 : numDirs.hashCode());
        result = prime * result + ((numNonDirFiles == null) ? 0 : numNonDirFiles.hashCode());
        result = prime * result + ((scanSourceType == null) ? 0 : scanSourceType.hashCode());
        result = prime * result + ((scannerVersion == null) ? 0 : scannerVersion.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        ScanHistoryItem other = (ScanHistoryItem) obj;
        if (createdByUserName == null) {
            if (other.createdByUserName != null) {
                return false;
            }
        } else if (!createdByUserName.equals(other.createdByUserName)) {
            return false;
        }
        if (createdOn == null) {
            if (other.createdOn != null) {
                return false;
            }
        } else if (!createdOn.equals(other.createdOn)) {
            return false;
        }
        if (lastModifiedOn == null) {
            if (other.lastModifiedOn != null) {
                return false;
            }
        } else if (!lastModifiedOn.equals(other.lastModifiedOn)) {
            return false;
        }
        if (numDirs == null) {
            if (other.numDirs != null) {
                return false;
            }
        } else if (!numDirs.equals(other.numDirs)) {
            return false;
        }
        if (numNonDirFiles == null) {
            if (other.numNonDirFiles != null) {
                return false;
            }
        } else if (!numNonDirFiles.equals(other.numNonDirFiles)) {
            return false;
        }
        if (scanSourceType == null) {
            if (other.scanSourceType != null) {
                return false;
            }
        } else if (!scanSourceType.equals(other.scanSourceType)) {
            return false;
        }
        if (scannerVersion == null) {
            if (other.scannerVersion != null) {
                return false;
            }
        } else if (!scannerVersion.equals(other.scannerVersion)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ScanHistoryItem [scannerVersion=");
        builder.append(scannerVersion);
        builder.append(", lastModifiedOn=");
        builder.append(lastModifiedOn);
        builder.append(", createdOn=");
        builder.append(createdOn);
        builder.append(", createdByUserName=");
        builder.append(createdByUserName);
        builder.append(", status=");
        builder.append(status);
        builder.append(", scanSourceType=");
        builder.append(scanSourceType);
        builder.append(", numDirs=");
        builder.append(numDirs);
        builder.append(", numNonDirFiles=");
        builder.append(numNonDirFiles);
        builder.append("]");
        return builder.toString();
    }

}
