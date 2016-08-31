package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationCountBuilder {

	private final ProjectVersion projectVersion;
	private final Date startDate;
	private final Date endDate;
	private int policyViolationCount;
	private int policyOverrideCount;
	private int totalVulnCount;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;
	private final Set<PolicyRule> policyRuleSet;

	public NotificationCountBuilder() {
		projectVersion = new ProjectVersion();
		this.startDate = new Date();
		this.endDate = new Date();
		policyRuleSet = new HashSet<>();
	}

	private NotificationCountBuilder(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final int policyViolationCount, final int policyOverrideCount, final Set<PolicyRule> policyRuleSet,
			final int totalVulnCount, final int addedVulnCount, final int updatedVulnCount,
			final int deletedVulnCount) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.policyViolationCount = policyViolationCount;
		this.policyOverrideCount = policyOverrideCount;
		this.totalVulnCount = totalVulnCount;
		this.addedVulnCount = addedVulnCount;
		this.updatedVulnCount = updatedVulnCount;
		this.deletedVulnCount = deletedVulnCount;
		this.policyRuleSet = policyRuleSet;
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
		return policyViolationCount;
	}

	public int getPolicyOverrideCount() {
		return policyOverrideCount;
	}

	private int calculateTotal() {
		return policyViolationCount + policyOverrideCount + totalVulnCount;
	}

	public NotificationCountBuilder updateProjectVersion(final ProjectVersion projectVersion) {
		return new NotificationCountBuilder(startDate, endDate, projectVersion, policyViolationCount,
				policyOverrideCount, policyRuleSet, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public NotificationCountBuilder updateDateRange(final Date startDate, final Date endDate) {
		return new NotificationCountBuilder(startDate, endDate, projectVersion, policyViolationCount,
				policyOverrideCount, policyRuleSet, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public NotificationCountData build() {
		return new NotificationCountData(startDate, endDate, projectVersion, calculateTotal(), policyViolationCount,
				policyOverrideCount, policyRuleSet, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	private void addPolicyRules(final Collection<PolicyRule> rules) {
		policyRuleSet.addAll(rules);
	}

	public void increment(final NotificationContentItem item) {
		if (item instanceof PolicyOverrideContentItem) {
			incrementPolicyOverrideCounts((PolicyOverrideContentItem) item);
		} else if (item instanceof PolicyViolationContentItem) {
			incrementPolicyCounts((PolicyViolationContentItem) item);
		} else if (item instanceof VulnerabilityContentItem) {
			incrementVulnerabilityCounts((VulnerabilityContentItem) item);
		}
	}

	private void incrementPolicyCounts(final PolicyViolationContentItem item) {
		policyViolationCount++;
		addPolicyRules(item.getPolicyRuleList());
	}

	private void incrementPolicyOverrideCounts(final PolicyOverrideContentItem item) {
		policyOverrideCount++;
		addPolicyRules(item.getPolicyRuleList());
	}

	private void incrementVulnerabilityCounts(final VulnerabilityContentItem item) {
		totalVulnCount++;
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
