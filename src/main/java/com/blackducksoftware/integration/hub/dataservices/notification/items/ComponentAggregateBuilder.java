package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;

public class ComponentAggregateBuilder {

	private String componentName;
	private String componentVersion;
	private String componentId;
	private String componentVersionId;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;
	private final List<PolicyViolationContentItem> policyViolationSet;
	private final List<PolicyOverrideContentItem> policyOverrideSet;
	private final List<VulnerabilityContentItem> vulnerabilityList;
	private VulnerabilityRestService restService;

	public ComponentAggregateBuilder() {
		policyViolationSet = new ArrayList<>();
		policyOverrideSet = new ArrayList<>();
		vulnerabilityList = new ArrayList<>();
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(final String componentName) {
		this.componentName = componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public void setComponentVersion(final String componentVersion) {
		this.componentVersion = componentVersion;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(final String componentId) {
		this.componentId = componentId;
	}

	public String getComponentVersionId() {
		return componentVersionId;
	}

	public void setComponentVersionId(final String componentVersionId) {
		this.componentVersionId = componentVersionId;
	}

	public VulnerabilityRestService getRestService() {
		return restService;
	}

	public void setRestService(final VulnerabilityRestService restService) {
		this.restService = restService;
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
		List<VulnerabilityItem> itemList = new ArrayList<>();
		final ComponentVulnerabilitySummaryBuilder summaryBuilder = new ComponentVulnerabilitySummaryBuilder();
		if (getRestService() != null) {
			try {
				itemList = getRestService().getComponentVersionVulnerabilities(getComponentId(),
						getComponentVersionId());
			} catch (IOException | URISyntaxException | BDRestException e) {
			}
		}
		summaryBuilder.setComponentName(getComponentName());
		summaryBuilder.setComponentVersion(getComponentVersion());
		summaryBuilder.setVulnerabilityList(itemList);
		final ComponentVulnerabilitySummary vulnSummary = summaryBuilder.build();
		return new ComponentAggregateData(getComponentName(), getComponentVersion(), policyViolationSet,
				policyOverrideSet, vulnerabilityList, addedVulnCount, updatedVulnCount, deletedVulnCount, vulnSummary);
	}

}
