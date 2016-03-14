package com.blackducksoftware.integration.hub.scan.status;

public class ScanStatusMeta {
    private final String href;

    public ScanStatusMeta(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
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
        if (!(obj instanceof ScanStatusMeta)) {
            return false;
        }
        ScanStatusMeta other = (ScanStatusMeta) obj;
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ScanStatusMeta [href=");
        builder.append(href);
        builder.append("]");
        return builder.toString();
    }

}
