package com.blackducksoftware.integration.hub.util;

public class ProjectNameVersionGuess {
    private String projectName;
    private String versionName;

    public ProjectNameVersionGuess(String projectName, String versionName) {
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
