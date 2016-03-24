package com.blackducksoftware.integration.hub.scan.api;

import java.util.List;

public class ScanLocationResults {

    private Integer totalCount; // Number of results

    private List<ScanLocationItem> items;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<ScanLocationItem> getItems() {
        return items;
    }

    public void setItems(List<ScanLocationItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ScanLocationResults [totalCount=");
        builder.append(totalCount);
        builder.append(", items=");
        builder.append(items);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + ((totalCount == null) ? 0 : totalCount.hashCode());
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
        if (!(obj instanceof ScanLocationResults)) {
            return false;
        }
        ScanLocationResults other = (ScanLocationResults) obj;
        if (items == null) {
            if (other.items != null) {
                return false;
            }
        } else if (!items.equals(other.items)) {
            return false;
        }
        if (totalCount == null) {
            if (other.totalCount != null) {
                return false;
            }
        } else if (!totalCount.equals(other.totalCount)) {
            return false;
        }
        return true;
    }

}
