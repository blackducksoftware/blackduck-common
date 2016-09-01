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
	private final List<PolicyViolationContentItem> policyViolationList;
	private final List<PolicyOverrideContentItem> policyOverrideList;
	private final List<VulnerabilityContentItem> vulnerabilityList;

	public ProjectAggregateData(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final List<PolicyViolationContentItem> policyViolationList,
			final List<PolicyOverrideContentItem> policyOverrideList,
			final List<VulnerabilityContentItem> vulnerabilityList, final int vulnAddedCount,
			final int vulnUpdatedCount, final int vulnDeletedCount) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.vulnAddedCount = vulnAddedCount;
		this.vulnUpdatedCount = vulnUpdatedCount;
		this.vulnDeletedCount = vulnDeletedCount;
		this.policyViolationList = policyViolationList;
		this.policyOverrideList = policyOverrideList;
		this.vulnerabilityList = vulnerabilityList;
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
		return getPolicyViolationCount() + getPolicyOverrideCount() + getVulnerabilityCount();
	}

	public int getPolicyViolationCount() {
		if (policyViolationList == null) {
			return 0;
		} else {
			return policyViolationList.size();
		}
	}

	public int getPolicyOverrideCount() {
		if (policyOverrideList == null) {
			return 0;
		} else {
			return policyOverrideList.size();
		}
	}

	public int getVulnerabilityCount() {
		if (vulnerabilityList == null) {
			return 0;
		} else {
			return vulnerabilityList.size();
		}
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

	public List<PolicyViolationContentItem> getPolicyViolationList() {
		return policyViolationList;
	}

	public List<PolicyOverrideContentItem> getPolicyOverrideList() {
		return policyOverrideList;
	}

	public List<VulnerabilityContentItem> getVulnerabilityList() {
		return vulnerabilityList;
	}

	@Override
	public String toString() {
		return "NotificationCountData [startDate=" + startDate + ", endDate=" + endDate + ", projectVersion="
				+ projectVersion + ", total=" + getTotal() + ", policyViolationCount=" + getPolicyViolationCount()
				+ ", policyOverrideCount=" + getPolicyOverrideCount() + ", vulnerabilityCount="
				+ getVulnerabilityCount() + ", vulnAddedCount=" + vulnAddedCount + ", vulnUpdatedCount="
				+ vulnUpdatedCount + ", vulnDeletedCount=" + vulnDeletedCount + ", policyViolationList="
				+ policyViolationList + ", policyOverrideList=" + policyOverrideList + ", vulnerabilityList="
				+ vulnerabilityList + "]";
	}
}
