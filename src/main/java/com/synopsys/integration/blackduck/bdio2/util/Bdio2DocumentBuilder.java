package com.synopsys.integration.blackduck.bdio2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;
import com.sun.tools.javac.code.Attribute;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;

public class Bdio2DocumentBuilder {
    private final Map<ExternalId, Project> existingSubprojects = new HashMap<>();
    private final Map<ExternalId, Component> existingComponents = new HashMap<>();

    public Component getOrComputeComponentIfAbsent(Dependency dependency, Consumer<Component> ifAbsent) {
        if (existingComponents.containsKey(dependency.getExternalId())) {
            return existingComponents.get(dependency.getExternalId());
        } else {
            Component component = componentFromDependency(dependency);
            existingComponents.put(dependency.getExternalId(), component);
            ifAbsent.accept(component);
            return component;
        }
    }

    public Project getOrComputeProjectIfAbsent(Dependency dependency, Consumer<Project> ifAbsent) {
        if (existingSubprojects.containsKey(dependency.getExternalId())) {
            return existingSubprojects.get(dependency.getExternalId());
        } else {
            Project project = projectFromDependency(dependency);
            existingSubprojects.put(dependency.getExternalId(), project);
            ifAbsent.accept(project);
            return project;
        }
    }

    private Project projectFromDependency(final Dependency dependency) {
        return new Project(dependency.getExternalId().createBdioId().toString())
                   .name(dependency.getName())
                   .version(dependency.getVersion())
                   .identifier(dependency.getExternalId().createExternalId())
                   .namespace(dependency.getExternalId().getForge().getName());
    }

    private Component componentFromDependency(final Dependency dependency) {
        return new Component(dependency.getExternalId().createBdioId().toString())
                   .name(dependency.getName())
                   .version(dependency.getVersion())
                   .identifier(dependency.getExternalId().createExternalId())
                   .namespace(dependency.getExternalId().getForge().getName());
    }

    public List<Project> getEncounteredProjects() {
        return new ArrayList<>(existingSubprojects.values());
    }

    public List<Component> getEncounteredComponents() {
        return new ArrayList<>(existingComponents.values());
    }
}
