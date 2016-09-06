package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.ArrayList;
import java.util.List;

public class ComponentAggregateBuilder {

	private String componentName;
	private String componentVersion;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;
	private final List<PolicyViolationContentItem> policyViolationSet;
	private final List<PolicyOverrideContentItem> policyOverrideSet;
	private final List<VulnerabilityContentItem> vulnerabilityList;

	public ComponentAggregateBuilder() {
		policyViolationSet = new ArrayList<>();
		policyOverrideSet = new ArrayList<>();
		vulnerabilityList = new ArrayList<>();
	}

	public int getTotal() {
		return getPolicyViolationCount() + getPolicyOverrideCount() + getVulnerabilityCount();
	}

	public int getAddedVulnCount() {
		return addedVulnCount;
	}

	public void setAddedVulnCount(final int addedVulnCount) {
		this.addedVulnCount = addedVulnCount;
	}

	public int getUpdatedVulnCount() {
		return updatedVulnCount;
	}

	public void setUpdatedVulnCount(final int updatedVulnCount) {
		this.updatedVulnCount = updatedVulnCount;
	}

	public int getDeletedVulnCount() {
		return deletedVulnCount;
	}

	public void setDeletedVulnCount(final int deletedVulnCount) {
		this.deletedVulnCount = deletedVulnCount;
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

	public ComponentAggregateData build() {
		return new ComponentAggregateData(componentName, componentVersion, policyViolationSet, policyOverrideSet,
				vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}
}
