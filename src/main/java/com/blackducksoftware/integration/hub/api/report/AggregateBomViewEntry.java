/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.api.report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.report.risk.RiskCounts;
import com.blackducksoftware.integration.hub.api.report.risk.RiskProfile;
import com.blackducksoftware.integration.hub.model.type.VersionBomPolicyStatusOverallStatusEnum;

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

    private final String since;

    private final ProjectData producerProject;

    private final List<ReleaseData> producerReleases;

    private final List<LicenseDefinition> licenses;

    private final RiskProfile riskProfile;

    private final String policyApprovalStatus;

    public AggregateBomViewEntry(// NOPMD ExcessiveParameterList
            final List<String> bomEntryIds, final List<Long> bomViewEntryIds, final List<String> matchTypes,
            final List<String> producerMatchTypes, final List<String> componentMatchTypes, final List<String> usages,
            final List<Boolean> inUses, final List<UserData> createdByUsers, final String since,
            final ProjectData producerProject, final List<ReleaseData> producerReleases,
            final List<LicenseDefinition> licenses, final RiskProfile riskProfile, final String policyApprovalStatus) {
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
        this.policyApprovalStatus = policyApprovalStatus;
    }

    public String getPolicyApprovalStatus() {
        return policyApprovalStatus;
    }

    public VersionBomPolicyStatusOverallStatusEnum getPolicyApprovalStatusEnum() {
        return VersionBomPolicyStatusOverallStatusEnum.valueOf(policyApprovalStatus);
    }

    public List<String> getBomEntryIds() {
        return bomEntryIds;
    }

    public List<UUID> getBomEntryUUIds() {
        final List<UUID> bomEntryUUIds = new ArrayList<>();
        for (final String bomEntryId : bomEntryIds) {
            if (StringUtils.isBlank(bomEntryId)) {
                continue;
            }
            try {
                bomEntryUUIds.add(UUID.fromString(bomEntryId));
            } catch (final IllegalArgumentException e) {
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
        // The first license should be the "parent license" and it should have
        // the correct display of all the licenses
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
        if (riskProfile == null || riskProfile.getCategories() == null
                || riskProfile.getCategories().getVULNERABILITY() == null) {
            return null;
        }
        return riskProfile.getCategories().getVULNERABILITY();
    }

    public RiskCounts getActivityRisk() {
        if (riskProfile == null || riskProfile.getCategories() == null
                || riskProfile.getCategories().getACTIVITY() == null) {
            return null;
        }
        return riskProfile.getCategories().getACTIVITY();
    }

    public RiskCounts getVersionRisk() {
        if (riskProfile == null || riskProfile.getCategories() == null
                || riskProfile.getCategories().getVERSION() == null) {
            return null;
        }
        return riskProfile.getCategories().getVERSION();
    }

    public RiskCounts getLicenseRisk() {
        if (riskProfile == null || riskProfile.getCategories() == null
                || riskProfile.getCategories().getLICENSE() == null) {
            return null;
        }
        return riskProfile.getCategories().getLICENSE();
    }

    /**
     * Returns the appropriate String for the License Risk type to be used in
     * the UI. If the License Risk is null it will return an empty String.
     *
     */
    public String getLicenseRiskString() {
        if (getLicenseRisk() == null) {
            return "";
        }
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
        if (riskProfile == null || riskProfile.getCategories() == null
                || riskProfile.getCategories().getOPERATIONAL() == null) {
            return null;
        }
        return riskProfile.getCategories().getOPERATIONAL();
    }

    /**
     * Returns the appropriate String for the Operational Risk type to be used
     * in the UI. If the Operational Risk is null it will return an empty
     * String.
     *
     */
    public String getOperationalRiskString() {
        if (getOperationalRisk() == null) {
            return "";
        }
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
        } catch (final IllegalArgumentException e) {
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
        // There should only be a single producer release
        if (producerReleases == null || producerReleases.isEmpty()) {
            return "";
        }
        return producerReleases.get(0).getId();
    }

    public String getProducerReleasesDisplay() {
        // There should only be a single producer release
        if (producerReleases == null || producerReleases.isEmpty()) {
            return "";
        }
        return producerReleases.get(0).getVersion();
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
        result = prime * result + ((policyApprovalStatus == null) ? 0 : policyApprovalStatus.hashCode());
        result = prime * result + ((producerMatchTypes == null) ? 0 : producerMatchTypes.hashCode());
        result = prime * result + ((producerProject == null) ? 0 : producerProject.hashCode());
        result = prime * result + ((producerReleases == null) ? 0 : producerReleases.hashCode());
        result = prime * result + ((riskProfile == null) ? 0 : riskProfile.hashCode());
        result = prime * result + ((since == null) ? 0 : since.hashCode());
        result = prime * result + ((usages == null) ? 0 : usages.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AggregateBomViewEntry)) {
            return false;
        }
        final AggregateBomViewEntry other = (AggregateBomViewEntry) obj;
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
        if (policyApprovalStatus == null) {
            if (other.policyApprovalStatus != null) {
                return false;
            }
        } else if (!policyApprovalStatus.equals(other.policyApprovalStatus)) {
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
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
