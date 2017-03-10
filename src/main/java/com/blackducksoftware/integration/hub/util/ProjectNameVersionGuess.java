package com.blackducksoftware.integration.hub.util;

public class ProjectNameVersionGuess {
    private final String projectName;

    private final String versionName;

    public ProjectNameVersionGuess(final String projectName, final String versionName) {
        this.projectName = projectName;
        this.versionName = versionName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

}
