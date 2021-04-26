package com.synopsys.integration.blackduck.bdio2.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

public class Bdio2Project {
    private final Project project;
    private final List<Component> components;
    private final Collection<Bdio2Project> subprojects;

    public Bdio2Project(final Project project, final List<Component> components) {
        this(project, components, Collections.emptyList());
    }

    public Bdio2Project(final Project project, final List<Component> components, final Collection<Bdio2Project> subprojects) {
        this.project = project;
        this.components = components;
        this.subprojects = subprojects;
    }

    public Project getProject() {
        return project;
    }

    public Collection<Bdio2Project> getSubprojects() {
        return subprojects;
    }

    public List<Component> getComponents() {
        return components;
    }
}
