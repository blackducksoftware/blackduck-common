package com.blackducksoftware.integration.hub.report.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.report.risk.api.RiskCounts;
import com.blackducksoftware.integration.hub.report.risk.api.RiskProfile;

/**
 * This is a view of an aggregation of BomEntries.
 *
 */
public class AggregateBomViewEntry {
    private final List<String> bomEntryIds;

    private final List<Long> bomViewEntryIds;

    private final List<String> matchTypes;

    /**
     * e.g. MANUAL, AUTO
     */
    private final List<String> producerMatchTypes;

    private final List<String> componentMatchTypes;

    private final List<String> usages;

    private final List<Boolean> inUses;

    private final List<UserData> createdByUsers;

    /**
     * minimal of since of all entries
     */
    private final String since;

    private final ProjectData producerProject;

    private final List<ReleaseData> producerReleases;

    private final List<LicenseDefinition> licenses;

    private final RiskProfile riskProfile;

    public AggregateBomViewEntry(// NOPMD ExcessiveParameterList
            List<String> bomEntryIds,
            List<Long> bomViewEntryIds,
            List<String> matchTypes,
            List<String> producerMatchTypes,
            List<String> componentMatchTypes,
            List<String> usages,
            List<Boolean> inUses,
            List<UserData> createdByUsers,
            String since,
            ProjectData producerProject,
            List<ReleaseData> producerReleases,
            List<LicenseDefinition> licenses,
            RiskProfile riskProfile) {
        this.bomEntryIds = bomEntryIds;
        this.bomViewEntryIds = bomViewEntryIds;
        this.matchTypes = matchTypes;
        this.producerMatchTypes = producerMatchTypes;
        this.componentMatchTypes = componentMatchTypes;
        this.usages = usages;
        this.inUses = inUses;
        this.createdByUsers = createdByUsers;
        this.since = since;
        this.producerProject = producerProject;
        this.producerReleases = producerReleases;
        this.licenses = licenses;
        this.riskProfile = riskProfile;
    }

    public List<String> getBomEntryIds() {
        return bomEntryIds;
    }

    public List<UUID> getBomEntryUUIds() {
        List<UUID> bomEntryUUIds = new ArrayList<UUID>();
        for (String bomEntryId : bomEntryIds) {
            if (StringUtils.isBlank(bomEntryId)) {
                continue;
            }
            try {
                bomEntryUUIds.add(UUID.fromString(bomEntryId));
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return bomEntryUUIds;
    }

    public List<Long> getBomViewEntryIds() {
        return bomViewEntryIds;
    }

    public List<String> getMatchTypes() {
        return matchTypes;
    }

    public List<String> getProducerMatchTypes() {
        return producerMatchTypes;
    }

    public List<String> getComponentMatchTypes() {
        return componentMatchTypes;
    }

    public List<String> getUsages() {
        return usages;
    }

    public List<Boolean> getInUses() {
        return inUses;
    }

    public List<UserData> getCreatedByUsers() {
        return createdByUsers;
    }

    public String getSince() {
        return since;
    }

    public List<LicenseDefinition> getLicenses() {
        return licenses;
    }

    public String getLicensesDisplay() {
        // The first license should be the "parent license" and it should have the correct display of all the licenses
        // for this entry
        if (licenses == null || licenses.isEmpty()) {
            return "";
        }
        return licenses.get(0).getLicenseDisplay();
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public RiskCounts getVulnerabilityRisk() {
        return riskProfile.getCategories().getVULNERABILITY();
    }

    public RiskCounts getActivityRisk() {
        return riskProfile.getCategories().getACTIVITY();
    }

    public RiskCounts getVersionRisk() {
        return riskProfile.getCategories().getVERSION();
    }

    public RiskCounts getLicenseRisk() {
        return riskProfile.getCategories().getLICENSE();
    }

    /**
     * Returns the appropriate String for the License Risk type to be used in the UI
     *
     */
    public String getLicenseRiskString() {
        if (getLicenseRisk().getHIGH() != 0) {
            return "H";
        } else if (getLicenseRisk().getMEDIUM() != 0) {
            return "M";
        } else if (getLicenseRisk().getLOW() != 0) {
            return "L";
        } else {
            return "-";
        }
    }

    public RiskCounts getOperationalRisk() {
        return riskProfile.getCategories().getOPERATIONAL();
    }

    /**
     * Returns the appropriate String for the Operational Risk type to be used in the UI
     *
     */
    public String getOperationalRiskString() {
        if (getOperationalRisk().getHIGH() != 0) {
            return "H";
        } else if (getOperationalRisk().getMEDIUM() != 0) {
            return "M";
        } else if (getOperationalRisk().getLOW() != 0) {
            return "L";
        } else {
            return "-";
        }
    }

    public DateTime getSinceTime() {
        if (StringUtils.isBlank(since)) {
            return null;
        }
        try {
            return new DateTime(since);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ProjectData getProducerProject() {
        return producerProject;
    }

    public List<ReleaseData> getProducerReleases() {
        return producerReleases;
    }

    public String getProducerReleasesId() {
        // FIXME there should only be a single producer release
        if (producerReleases == null || producerReleases.isEmpty()) {
            return "";
        }

        return producerReleases.get(0).getId();
    }

    public String getProducerReleasesDisplay() {
        StringBuilder versionBuilder = new StringBuilder();
        for (ReleaseData release : producerReleases) {
            if (versionBuilder.length() > 0) {
                versionBuilder.append(", ");
            }
            versionBuilder.append(release.getVersion());
        }
        return versionBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bomEntryIds == null) ? 0 : bomEntryIds.hashCode());
        result = prime * result + ((bomViewEntryIds == null) ? 0 : bomViewEntryIds.hashCode());
        result = prime * result + ((componentMatchTypes == null) ? 0 : componentMatchTypes.hashCode());
        result = prime * result + ((createdByUsers == null) ? 0 : createdByUsers.hashCode());
        result = prime * result + ((inUses == null) ? 0 : inUses.hashCode());
        result = prime * result + ((licenses == null) ? 0 : licenses.hashCode());
        result = prime * result + ((matchTypes == null) ? 0 : matchTypes.hashCode());
        result = prime * result + ((producerMatchTypes == null) ? 0 : producerMatchTypes.hashCode());
        result = prime * result + ((producerProject == null) ? 0 : producerProject.hashCode());
        result = prime * result + ((producerReleases == null) ? 0 : producerReleases.hashCode());
        result = prime * result + ((riskProfile == null) ? 0 : riskProfile.hashCode());
        result = prime * result + ((since == null) ? 0 : since.hashCode());
        result = prime * result + ((usages == null) ? 0 : usages.hashCode());
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
        if (!(obj instanceof AggregateBomViewEntry)) {
            return false;
        }
        AggregateBomViewEntry other = (AggregateBomViewEntry) obj;
        if (bomEntryIds == null) {
            if (other.bomEntryIds != null) {
                return false;
            }
        } else if (!bomEntryIds.equals(other.bomEntryIds)) {
            return false;
        }
        if (bomViewEntryIds == null) {
            if (other.bomViewEntryIds != null) {
                return false;
            }
        } else if (!bomViewEntryIds.equals(other.bomViewEntryIds)) {
            return false;
        }
        if (componentMatchTypes == null) {
            if (other.componentMatchTypes != null) {
                return false;
            }
        } else if (!componentMatchTypes.equals(other.componentMatchTypes)) {
            return false;
        }
        if (createdByUsers == null) {
            if (other.createdByUsers != null) {
                return false;
            }
        } else if (!createdByUsers.equals(other.createdByUsers)) {
            return false;
        }
        if (inUses == null) {
            if (other.inUses != null) {
                return false;
            }
        } else if (!inUses.equals(other.inUses)) {
            return false;
        }
        if (licenses == null) {
            if (other.licenses != null) {
                return false;
            }
        } else if (!licenses.equals(other.licenses)) {
            return false;
        }
        if (matchTypes == null) {
            if (other.matchTypes != null) {
                return false;
            }
        } else if (!matchTypes.equals(other.matchTypes)) {
            return false;
        }
        if (producerMatchTypes == null) {
            if (other.producerMatchTypes != null) {
                return false;
            }
        } else if (!producerMatchTypes.equals(other.producerMatchTypes)) {
            return false;
        }
        if (producerProject == null) {
            if (other.producerProject != null) {
                return false;
            }
        } else if (!producerProject.equals(other.producerProject)) {
            return false;
        }
        if (producerReleases == null) {
            if (other.producerReleases != null) {
                return false;
            }
        } else if (!producerReleases.equals(other.producerReleases)) {
            return false;
        }
        if (riskProfile == null) {
            if (other.riskProfile != null) {
                return false;
            }
        } else if (!riskProfile.equals(other.riskProfile)) {
            return false;
        }
        if (since == null) {
            if (other.since != null) {
                return false;
            }
        } else if (!since.equals(other.since)) {
            return false;
        }
        if (usages == null) {
            if (other.usages != null) {
                return false;
            }
        } else if (!usages.equals(other.usages)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AggregateBomViewEntry [bomEntryIds=");
        builder.append(bomEntryIds);
        builder.append(", bomViewEntryIds=");
        builder.append(bomViewEntryIds);
        builder.append(", matchTypes=");
        builder.append(matchTypes);
        builder.append(", producerMatchTypes=");
        builder.append(producerMatchTypes);
        builder.append(", componentMatchTypes=");
        builder.append(componentMatchTypes);
        builder.append(", usages=");
        builder.append(usages);
        builder.append(", inUses=");
        builder.append(inUses);
        builder.append(", createdByUsers=");
        builder.append(createdByUsers);
        builder.append(", since=");
        builder.append(since);
        builder.append(", producerProject=");
        builder.append(producerProject);
        builder.append(", producerReleases=");
        builder.append(producerReleases);
        builder.append(", licenses=");
        builder.append(licenses);
        builder.append(", riskProfile=");
        builder.append(riskProfile);
        builder.append("]");
        return builder.toString();
    }

}
