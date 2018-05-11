package com.blackducksoftware.integration.hub.service.model;

public class ProjectVersionDescription {
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersionUri;

    public ProjectVersionDescription(final String projectName, final String projectVersionName, final String projectVersionUri) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersionUri = projectVersionUri;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersionUri() {
        return projectVersionUri;
    }

}
