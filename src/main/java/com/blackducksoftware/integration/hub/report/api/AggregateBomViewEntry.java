package com.blackducksoftware.integration.hub.report.api;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.build.MatchType;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.mapping.EntityItem;

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

    /**
     * TODO You have producer project, producer release, but for consumer, there are only entity keys, are these entity
     * key release, project, or code locations?
     */
    private final Set<EntityItem> consumers;

    /**
     * producerProject is shared by all agg entries
     */
    private final ProjectItem producerProject;

    private final SortedSet<ReleaseItem> producerReleases;

    private final SortedSet<LicenseDefinition> licenses;

    private final Optional<VersionData> versionRisk;

    private final Optional<ActivityData> activity;

    private final Optional<VulnerabilityData> vulnerability;

    private final Set<AppliedAdjustmentData> appliedAdjustments;

    private RiskProfile riskProfile;

    @JsonCreator
    public AggregateBomViewEntry(// NOPMD ExcessiveParameterList
            @JsonProperty("bomEntryIds") Set<UUID> bomEntryIds,
            @JsonProperty("bomViewEntryIds") Set<Long> bomViewEntryIds,
            @JsonProperty("matchTypes") SortedSet<MatchType> matchTypes,
            @JsonProperty("creationMethods") SortedSet<ProducerMatchType> producerMatchTypes,
            @JsonProperty("componentMatchTypes") SortedSet<ProducerMatchType> componentMatchTypes,
            @JsonProperty("usages") SortedSet<Usage> usages,
            @JsonProperty("inUses") SortedSet<Boolean> inUses,
            @JsonProperty("createdBys") SortedSet<UserData> createdByUsers,
            @JsonProperty("since") DateTime since,
            @JsonProperty("consumers") Set<EntityKey> consumers,
            @JsonProperty("producerProject") ProjectData producerProject,
            @JsonProperty("producerReleases") SortedSet<ReleaseData> producerReleases,
            @JsonProperty("scans") SortedSet<ScanData> scans,
            @JsonProperty("licenses") SortedSet<LicenseDefinition> licenses,
            @JsonProperty("versionRisk") VersionData versionRisk,
            @JsonProperty("activity") ActivityData activity,
            @JsonProperty("vulnerability") VulnerabilityData vulnerability,
            @JsonProperty("appliedAdjustments") Set<AppliedAdjustmentData> appliedAdjustments,
            @JsonProperty("riskProfile") RiskProfile riskProfile) {
        this.bomEntryIds = bomEntryIds;
        this.bomViewEntryIds = bomViewEntryIds;
        this.matchTypes = matchTypes;
        this.producerMatchTypes = producerMatchTypes;
        this.componentMatchTypes = componentMatchTypes;
        this.usages = usages;
        this.inUses = inUses;
        this.createdByUsers = createdByUsers;
        this.since = since;
        this.consumers = consumers;
        this.producerProject = producerProject;
        this.producerReleases = producerReleases;
        this.scans = scans;
        this.licenses = licenses;
        this.versionRisk = Optional.fromNullable(versionRisk);
        this.activity = Optional.fromNullable(activity);
        this.vulnerability = Optional.fromNullable(vulnerability);
        this.appliedAdjustments = appliedAdjustments;
        this.riskProfile = riskProfile;
    }

    public Set<UUID> getBomEntryIds() {
        return bomEntryIds;
    }

    @Override
    public Set<Long> getBomViewEntryIds() {
        return bomViewEntryIds;
    }

    public SortedSet<MatchType> getMatchTypes() {
        return matchTypes;
    }

    public SortedSet<ProducerMatchType> getProducerMatchTypes() {
        return producerMatchTypes;
    }

    public SortedSet<ProducerMatchType> getComponentMatchTypes() {
        return componentMatchTypes;
    }

    public SortedSet<Usage> getUsages() {
        return usages;
    }

    public SortedSet<Boolean> getInUses() {
        return inUses;
    }

    public SortedSet<UserData> getCreatedByUsers() {
        return createdByUsers;
    }

    public DateTime getSince() {
        return since;
    }

    public Set<EntityKey> getConsumers() {
        return consumers;
    }

    /**
     * Required for sorting by Spring
     */
    @JsonIgnore
    public int getUsedCount() {
        return consumers != null ? consumers.size() : 0;
    }

    /**
     * Required for sorting by Spring
     */
    @JsonIgnore
    public int getVersionsUsedCount() {
        return producerReleases != null ? producerReleases.size() : 0;
    }

    public ProjectData getProducerProject() {
        return producerProject;
    }

    public SortedSet<ReleaseData> getProducerReleases() {
        return producerReleases;
    }

    public SortedSet<ScanData> getScans() {
        return scans;
    }

    public SortedSet<LicenseDefinition> getLicenses() {
        return licenses;
    }

    public Optional<VersionData> getVersionRisk() {
        return versionRisk;
    }

    public Optional<ActivityData> getActivity() {
        return activity;
    }

    public Optional<VulnerabilityData> getVulnerability() {
        return vulnerability;
    }

    public Set<AppliedAdjustmentData> getAppliedAdjustments() {
        return appliedAdjustments;
    }

    /**
     * An aggregate entry can not be ignored, you can only ignore individual bomentries
     *
     * @return
     */

    public Optional<UUID> getIgnoreId() {
        return Optional.absent();
    }

    @Override
    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    @Override
    public void setRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
    }
}
