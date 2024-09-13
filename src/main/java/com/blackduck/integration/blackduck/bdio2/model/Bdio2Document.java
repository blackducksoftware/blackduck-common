/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2.model;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

import java.util.List;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Project project;
    private final List<Project> subProjects;
    private final List<Component> components;

    public Bdio2Document(BdioMetadata bdioMetadata, Project project, List<Project> subProjects, List<Component> components) {
        this.bdioMetadata = bdioMetadata;
        this.project = project;
        this.subProjects = subProjects;
        this.components = components;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Project getProject() {
        return project;
    }

    public List<Project> getSubProjects() {
        return subProjects;
    }

    public List<Component> getComponents() {
        return components;
    }
}
