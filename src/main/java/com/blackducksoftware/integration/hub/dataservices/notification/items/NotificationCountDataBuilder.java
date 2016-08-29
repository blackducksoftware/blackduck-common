package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationCountDataBuilder {

	private final ProjectVersion projectVersion;
	private final Date startDate;
	private final Date endDate;
	private int policyViolationCount;
	private int policyOverrideCount;
	private int totalVulnCount;
	private int addedVulnCount;
	private int updatedVulnCount;
	private int deletedVulnCount;

	public NotificationCountDataBuilder() {
		projectVersion = new ProjectVersion();
		this.startDate = new Date();
		this.endDate = new Date();
	}

	private NotificationCountDataBuilder(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final int policyViolationCount, final int policyOverrideCount, final int totalVulnCount,
			final int addedVulnCount, final int updatedVulnCount, final int deletedVulnCount) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.policyViolationCount = policyViolationCount;
		this.policyOverrideCount = policyOverrideCount;
		this.totalVulnCount = totalVulnCount;
		this.addedVulnCount = addedVulnCount;
		this.updatedVulnCount = updatedVulnCount;
		this.deletedVulnCount = deletedVulnCount;
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

	public NotificationCountDataBuilder updateProjectVersion(final ProjectVersion projectVersion) {
		return new NotificationCountDataBuilder(startDate, endDate, projectVersion, policyViolationCount,
				policyOverrideCount, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public NotificationCountDataBuilder updateDateRange(final Date startDate, final Date endDate) {
		return new NotificationCountDataBuilder(startDate, endDate, projectVersion, policyViolationCount,
				policyOverrideCount, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}

	public void incrementPolicyCounts(final RuleViolationNotificationItem item) {
		policyViolationCount++;
	}

	public void incrementPolicyOverrideCounts(final PolicyOverrideNotificationItem item) {
		policyOverrideCount++;
	}

	public void incrementVulnerabilityCounts(final VulnerabilityNotificationItem item) {
		totalVulnCount++;
		final VulnerabilityNotificationContent content = item.getContent();
		if (content != null) {
			addedVulnCount += content.getNewVulnerabilityCount();
			updatedVulnCount += content.getUpdatedVulnerabilityCount();
			deletedVulnCount += content.getDeletedVulnerabilityCount();
		}
	}

	public NotificationCountData build() {
		return new NotificationCountData(startDate, endDate, projectVersion, calculateTotal(), policyViolationCount,
				policyOverrideCount, totalVulnCount, addedVulnCount, updatedVulnCount, deletedVulnCount);
	}
}
