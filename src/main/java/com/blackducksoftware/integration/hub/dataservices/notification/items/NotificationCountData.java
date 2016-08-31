package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationCountData {

	private final Date startDate;
	private final Date endDate;
	private final ProjectVersion projectVersion;
	private final int total;
	private final int policyViolationCount;
	private final int policyOverrideCount;
	private final int vulnerabilityCount;
	private final int vulnAddedCount;
	private final int vulnUpdatedCount;
	private final int vulnDeletedCount;

	public NotificationCountData(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final int total, final int policyViolationCount, final int policyOverrideCount,
			final int vulnerabilityCount, final int vulnAddedCount, final int vulnUpdatedCount,
			final int vulnDeletedCount) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.total = total;
		this.policyViolationCount = policyViolationCount;
		this.policyOverrideCount = policyOverrideCount;
		this.vulnerabilityCount = vulnerabilityCount;
		this.vulnAddedCount = vulnAddedCount;
		this.vulnUpdatedCount = vulnUpdatedCount;
		this.vulnDeletedCount = vulnDeletedCount;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public int getTotal() {
		return total;
	}

	public int getPolicyViolationCount() {
		return policyViolationCount;
	}

	public int getPolicyOverrideCount() {
		return policyOverrideCount;
	}

	public int getVulnerabilityCount() {
		return vulnerabilityCount;
	}

	public int getVulnAddedCount() {
		return vulnAddedCount;
	}

	public int getVulnUpdatedCount() {
		return vulnUpdatedCount;
	}

	public int getVulnDeletedCount() {
		return vulnDeletedCount;
	}

	@Override
	public String toString() {
		return "NotificationCountData [startDate=" + startDate + ", endDate=" + endDate + ", projectVersion="
				+ projectVersion + ", total=" + total + ", policyViolationCount=" + policyViolationCount
				+ ", policyOverrideCount=" + policyOverrideCount + ", vulnerabilityCount=" + vulnerabilityCount
				+ ", vulnAddedCount=" + vulnAddedCount + ", vulnUpdatedCount=" + vulnUpdatedCount
				+ ", vulnDeletedCount=" + vulnDeletedCount + "]";
	}
}
