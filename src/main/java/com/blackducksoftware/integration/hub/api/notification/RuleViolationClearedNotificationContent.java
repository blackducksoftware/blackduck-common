package com.blackducksoftware.integration.hub.api.notification;

import java.util.List;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.google.gson.annotations.SerializedName;

public class RuleViolationClearedNotificationContent {
	private String projectName;
	private String projectVersionName;
	private int componentVersionsInViolation;
	private List<ComponentVersionStatus> componentVersionStatuses;

	@SerializedName("projectVersion")
	private String projectVersionLink;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public int getComponentVersionsInViolation() {
		return componentVersionsInViolation;
	}

	public List<ComponentVersionStatus> getComponentVersionStatuses() {
		return componentVersionStatuses;
	}

	public String getProjectVersionLink() {
		return projectVersionLink;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public void setProjectVersionName(final String projectVersionName) {
		this.projectVersionName = projectVersionName;
	}

	public void setComponentVersionsInViolation(final int componentVersionsInViolation) {
		this.componentVersionsInViolation = componentVersionsInViolation;
	}

	public void setComponentVersionStatuses(final List<ComponentVersionStatus> componentVersionStatuses) {
		this.componentVersionStatuses = componentVersionStatuses;
	}

	public void setProjectVersionLink(final String projectVersionLink) {
		this.projectVersionLink = projectVersionLink;
	}

	@Override
	public String toString() {
		return "RuleViolationClearedContent [projectName=" + projectName + ", projectVersionName=" + projectVersionName
				+ ", componentVersionsInViolation=" + componentVersionsInViolation + ", componentVersionStatuses="
				+ componentVersionStatuses + ", projectVersionLink=" + projectVersionLink + "]";
	}
}
