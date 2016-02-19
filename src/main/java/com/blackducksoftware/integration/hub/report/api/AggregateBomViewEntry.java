package com.blackducksoftware.integration.hub.report.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.report.risk.api.RiskProfile;

/**
 * This is a view of an aggregation of BomEntries.
 *
 *
 */
public class AggregateBomViewEntry {
    private final List<UUID> bomEntryIds;

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
    private final DateTime since;

    private final List<LicenseDefinition> licenses;

    private RiskProfile riskProfile;

    public AggregateBomViewEntry(// NOPMD ExcessiveParameterList
            List<UUID> bomEntryIds,
            List<Long> bomViewEntryIds,
            List<String> matchTypes,
            List<String> producerMatchTypes,
            List<String> componentMatchTypes,
            List<String> usages,
            List<Boolean> inUses,
            List<UserData> createdByUsers,
            DateTime since,
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
        this.licenses = licenses;
        this.riskProfile = riskProfile;
    }

    public List<UUID> getBomEntryIds() {
        return bomEntryIds;
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

    public DateTime getSince() {
        return since;
    }

    public List<LicenseDefinition> getLicenses() {
        return licenses;
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
    }
}
