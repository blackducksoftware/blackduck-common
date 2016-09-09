package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class ProjectAggregateData {

	private final Date startDate;
	private final Date endDate;
	private final ProjectVersion projectVersion;
	private final int vulnAddedCount;
	private final int vulnUpdatedCount;
	private final int vulnDeletedCount;
	private final int policyViolationCount;
	private final int policyOverrideCount;
	private final int vulnerabilityCount;
	private final int totalCount;
	private final List<ComponentAggregateData> componentList;

	public ProjectAggregateData(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final int policyViolationCount, final int policyOverrideCount, final int vulnerabilityCount,
			final int totalCount, final int vulnAddedCount, final int vulnUpdatedCount, final int vulnDeletedCount,
			final List<ComponentAggregateData> componentList) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.policyViolationCount = policyViolationCount;
		this.policyOverrideCount = policyOverrideCount;
		this.vulnerabilityCount = vulnerabilityCount;
		this.totalCount = totalCount;
		this.vulnAddedCount = vulnAddedCount;
		this.vulnUpdatedCount = vulnUpdatedCount;
		this.vulnDeletedCount = vulnDeletedCount;
		this.componentList = componentList;
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
		return totalCount;
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

	public int getPolicyViolationCount() {
		return policyViolationCount;
	}

	public int getPolicyOverrideCount() {
		return policyOverrideCount;
	}

	public int getVulnerabilityCount() {
		return vulnerabilityCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public List<ComponentAggregateData> getComponentList() {
		return componentList;
	}

	@Override
	public String toString() {
		return "ProjectAggregateData [startDate=" + startDate + ", endDate=" + endDate + ", projectVersion="
				+ projectVersion + ", vulnAddedCount=" + vulnAddedCount + ", vulnUpdatedCount=" + vulnUpdatedCount
				+ ", vulnDeletedCount=" + vulnDeletedCount + ", policyViolationCount=" + policyViolationCount
				+ ", policyOverrideCount=" + policyOverrideCount + ", vulnerabilityCount=" + vulnerabilityCount
				+ ", totalCount=" + totalCount + ", componentList=" + componentList + "]";
	}
}
