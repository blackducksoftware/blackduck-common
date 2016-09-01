package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class ProjectAggregateBuilder {

	private final ProjectVersion projectVersion;
	private final Date startDate;
	private final Date endDate;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;
	private final List<PolicyViolationContentItem> policyViolationSet;
	private final List<PolicyOverrideContentItem> policyOverrideSet;
	private final List<VulnerabilityContentItem> vulnerabilityList;

	public ProjectAggregateBuilder() {
		projectVersion = new ProjectVersion();
		this.startDate = new Date();
		this.endDate = new Date();
		policyViolationSet = new ArrayList<>();
		policyOverrideSet = new ArrayList<>();
		vulnerabilityList = new ArrayList<>();
	}

	private ProjectAggregateBuilder(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final List<PolicyViolationContentItem> policyViolationSet,
			final List<PolicyOverrideContentItem> policyOverrideSet,
			final List<VulnerabilityContentItem> vulnerabilityList, final int addedVulnCount,
			final int updatedVulnCount, final int deletedVulnCount) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.addedVulnCount = addedVulnCount;
		this.updatedVulnCount = updatedVulnCount;
		this.deletedVulnCount = deletedVulnCount;
		this.policyViolationSet = policyViolationSet;
		this.policyOverrideSet = policyOverrideSet;
		this.vulnerabilityList = vulnerabilityList;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public int getPolicyViolationCount() {
		if (policyViolationSet == null) {
			return 0;
		} else {
			return policyViolationSet.size();
		}
	}

	public int getPolicyOverrideCount() {
		if (policyOverrideSet == null) {
			return 0;
		} else {
			return policyOverrideSet.size();
		}
	}

	public int getVulnerabilityCount() {
		if (vulnerabilityList == null) {
			return 0;
		} else {
			return vulnerabilityList.size();
		}
	}

	public ProjectAggregateBuilder updateProjectVersion(final ProjectVersion projectVersion) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, policyViolationSet, policyOverrideSet,
				vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public ProjectAggregateBuilder updateDateRange(final Date startDate, final Date endDate) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, policyViolationSet, policyOverrideSet,
				vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public ProjectAggregateData build() {
		return new ProjectAggregateData(startDate, endDate, projectVersion, policyViolationSet, policyOverrideSet,
				vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	private void addPolicyViolation(final PolicyViolationContentItem item) {
		policyViolationSet.add(item);
	}

	private void addPolicyOverride(final PolicyOverrideContentItem item) {
		policyOverrideSet.add(item);
	}

	private void addVulnerability(final VulnerabilityContentItem item) {
		vulnerabilityList.add(item);
	}

	public void increment(final NotificationContentItem item) {
		if (item instanceof PolicyOverrideContentItem) {
			addPolicyOverride((PolicyOverrideContentItem) item);
		} else if (item instanceof PolicyViolationContentItem) {
			// order matters PolicyOverrideContentItem is a sub-class of
			// PolicyViolationContentItem need to check it first.
			addPolicyViolation((PolicyViolationContentItem) item);
		} else if (item instanceof VulnerabilityContentItem) {
			incrementVulnerabilityCounts((VulnerabilityContentItem) item);
		}
	}

	private void incrementVulnerabilityCounts(final VulnerabilityContentItem item) {
		addVulnerability(item);
		if (item.getAddedVulnList() != null) {
			addedVulnCount += item.getAddedVulnList().size();
		}

		if (item.getUpdatedVulnList() != null) {
			updatedVulnCount += item.getUpdatedVulnList().size();
		}

		if (item.getDeletedVulnList() != null) {
			deletedVulnCount += item.getDeletedVulnList().size();
		}
	}
}
