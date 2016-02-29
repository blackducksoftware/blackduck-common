package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

public class LicenseDefinition {

    private final UUID licenseId;

    private final String discoveredAs;

    private final String name;

    private final String spdxId;

    private final String ownership;

    private final String codeSharing;

    private final String licenseDisplay;

    public LicenseDefinition(UUID licenseId,
            String discoveredAs, String name, String spdxId,
            String ownership, String codeSharing,
            String licenseDisplay) {
        this.licenseId = licenseId;
        this.discoveredAs = discoveredAs;
        this.name = name;
        this.spdxId = spdxId;
        this.ownership = ownership;
        this.codeSharing = codeSharing;
        this.licenseDisplay = licenseDisplay;

    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public String getDiscoveredAs() {
        return discoveredAs;
    }

    public String getName() {
        return name;
    }

    public String getSpdxId() {
        return spdxId;
    }

    public String getOwnership() {
        return ownership;
    }

    public String getCodeSharing() {
        return codeSharing;
    }

    /**
     * This method is supposed to be called by JSON serializer only
     */
    public String getLicenseDisplay() {
        return licenseDisplay;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codeSharing == null) ? 0 : codeSharing.hashCode());
        result = prime * result + ((discoveredAs == null) ? 0 : discoveredAs.hashCode());
        result = prime * result + ((licenseId == null) ? 0 : licenseId.hashCode());
        result = prime * result + ((licenseDisplay == null) ? 0 : licenseDisplay.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((ownership == null) ? 0 : ownership.hashCode());
        result = prime * result + ((spdxId == null) ? 0 : spdxId.hashCode());
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
        if (!(obj instanceof LicenseDefinition)) {
            return false;
        }
        LicenseDefinition other = (LicenseDefinition) obj;
        if (codeSharing == null) {
            if (other.codeSharing != null) {
                return false;
            }
        } else if (!codeSharing.equals(other.codeSharing)) {
            return false;
        }
        if (discoveredAs == null) {
            if (other.discoveredAs != null) {
                return false;
            }
        } else if (!discoveredAs.equals(other.discoveredAs)) {
            return false;
        }
        if (licenseId == null) {
            if (other.licenseId != null) {
                return false;
            }
        } else if (!licenseId.equals(other.licenseId)) {
            return false;
        }
        if (licenseDisplay != other.licenseDisplay) {
            return false;
        }
        if (licenseDisplay == null) {
            if (other.licenseDisplay != null) {
                return false;
            }
        } else if (!licenseDisplay.equals(other.licenseDisplay)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (ownership == null) {
            if (other.ownership != null) {
                return false;
            }
        } else if (!ownership.equals(other.ownership)) {
            return false;
        }
        if (spdxId == null) {
            if (other.spdxId != null) {
                return false;
            }
        } else if (!spdxId.equals(other.spdxId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LicenseDefinition [licenseId=");
        builder.append(licenseId);
        builder.append(", discoveredAs=");
        builder.append(discoveredAs);
        builder.append(", name=");
        builder.append(name);
        builder.append(", spdxId=");
        builder.append(spdxId);
        builder.append(", ownership=");
        builder.append(ownership);
        builder.append(", codeSharing=");
        builder.append(codeSharing);
        builder.append(", licenseDisplay=");
        builder.append(licenseDisplay);
        builder.append("]");
        return builder.toString();
    }

}
