package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class ProjectAggregateBuilder {

	private final ProjectVersion projectVersion;
	private final Date startDate;
	private final Date endDate;
	private int addedVulnCount = 0;
	private int updatedVulnCount = 0;
	private int deletedVulnCount = 0;
	private int policyViolationCount = 0;
	private int policyOverrideCount = 0;
	private int vulnerabilityCount = 0;
	private int totalCount = 0;

	private final Map<String, ComponentAggregateBuilder> compBuilderMap;

	public ProjectAggregateBuilder() {
		projectVersion = new ProjectVersion();
		this.startDate = new Date();
		this.endDate = new Date();
		compBuilderMap = new HashMap<>();
	}

	private ProjectAggregateBuilder(final Date startDate, final Date endDate, final ProjectVersion projectVersion,
			final Map<String, ComponentAggregateBuilder> compBuilderMap) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectVersion = projectVersion;
		this.compBuilderMap = compBuilderMap;
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

	public ProjectAggregateBuilder updateProjectVersion(final ProjectVersion projectVersion) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, compBuilderMap);
	}

	public ProjectAggregateBuilder updateDateRange(final Date startDate, final Date endDate) {
		return new ProjectAggregateBuilder(startDate, endDate, projectVersion, compBuilderMap);
	}

	public ProjectAggregateData build() {
		final List<ComponentAggregateData> compList = getComponentList();
		return new ProjectAggregateData(startDate, endDate, projectVersion, policyViolationCount, policyOverrideCount,
				vulnerabilityCount, totalCount, addedVulnCount, updatedVulnCount, deletedVulnCount, compList);
	}

	public void increment(final NotificationContentItem item) {
		final String compKey = getComponentKey(item);
		ComponentAggregateBuilder compBuilder;
		if (compBuilderMap.containsKey(compKey)) {
			compBuilder = compBuilderMap.get(compKey);
		} else {

			compBuilder = new ComponentAggregateBuilder();
			compBuilderMap.put(compKey, compBuilder);
		}
		compBuilder.increment(item);
	}

	private String getComponentKey(final NotificationContentItem item) {
		return item.getComponentName() + "-" + item.getComponentVersion();
	}

	private List<ComponentAggregateData> getComponentList() {
		final List<ComponentAggregateData> compList = new ArrayList<>(compBuilderMap.size());

		for (final Map.Entry<String, ComponentAggregateBuilder> entry : compBuilderMap.entrySet()) {
			final ComponentAggregateData compData = entry.getValue().build();
			policyViolationCount += compData.getPolicyViolationCount();
			policyOverrideCount += compData.getPolicyOverrideCount();
			vulnerabilityCount += compData.getVulnerabilityCount();
			totalCount += compData.getTotal();
			addedVulnCount += compData.getVulnAddedCount();
			updatedVulnCount += compData.getVulnUpdatedCount();
			deletedVulnCount += compData.getVulnDeletedCount();
			compList.add(compData);
		}

		return compList;
	}
}
