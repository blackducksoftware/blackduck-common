package com.blackducksoftware.integration.hub.response.mapping;

import java.util.List;

public class ScanLocationItem {

    private String id;

    private String scanId;

    private String host;

    private String path;

    private String scanInitiatorName;

    private String lastScanUploadDate;

    private String scanTime;

    private List<AssetReferenceItem> assetReferenceList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getScanInitiatorName() {
        return scanInitiatorName;
    }

    public void setScanInitiatorName(String scanInitiatorName) {
        this.scanInitiatorName = scanInitiatorName;
    }

    public String getLastScanUploadDate() {
        return lastScanUploadDate;
    }

    public void setLastScanUploadDate(String lastScanUploadDate) {
        this.lastScanUploadDate = lastScanUploadDate;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public List<AssetReferenceItem> getAssetReferenceList() {
        return assetReferenceList;
    }

    public void setAssetReferenceList(List<AssetReferenceItem> assetReferenceList) {
        this.assetReferenceList = assetReferenceList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ScanLocationResults [id=");
        builder.append(id);
        builder.append(", scanId=");
        builder.append(scanId);
        builder.append(", host=");
        builder.append(host);
        builder.append(", path=");
        builder.append(path);
        builder.append(", scanInitiatorName=");
        builder.append(scanInitiatorName);
        builder.append(", lastScanUploadDate=");
        builder.append(lastScanUploadDate);
        builder.append(", scanTime=");
        builder.append(scanTime);
        builder.append(", assetReferenceList=");
        builder.append(assetReferenceList);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assetReferenceList == null) ? 0 : assetReferenceList.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lastScanUploadDate == null) ? 0 : lastScanUploadDate.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((scanId == null) ? 0 : scanId.hashCode());
        result = prime * result + ((scanInitiatorName == null) ? 0 : scanInitiatorName.hashCode());
        result = prime * result + ((scanTime == null) ? 0 : scanTime.hashCode());
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
        ScanLocationItem other = (ScanLocationItem) obj;
        if (assetReferenceList == null) {
            if (other.assetReferenceList != null) {
                return false;
            }
        } else if (!assetReferenceList.equals(other.assetReferenceList)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (lastScanUploadDate == null) {
            if (other.lastScanUploadDate != null) {
                return false;
            }
        } else if (!lastScanUploadDate.equals(other.lastScanUploadDate)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (scanId == null) {
            if (other.scanId != null) {
                return false;
            }
        } else if (!scanId.equals(other.scanId)) {
            return false;
        }
        if (scanInitiatorName == null) {
            if (other.scanInitiatorName != null) {
                return false;
            }
        } else if (!scanInitiatorName.equals(other.scanInitiatorName)) {
            return false;
        }
        if (scanTime == null) {
            if (other.scanTime != null) {
                return false;
            }
        } else if (!scanTime.equals(other.scanTime)) {
            return false;
        }
        return true;
    }

}
