/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.util;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.BdioObject;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;
import com.blackducksoftware.common.value.Product;
import com.blackducksoftware.common.value.ProductList;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.dependency.ProjectDependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.bdio2.model.Bdio2Document;
import com.synopsys.integration.blackduck.bdio2.model.ProjectInfo;

public class Bdio2Factory {
    public static final List<Product> DEFAULT_PRODUCTS = Arrays.asList(Product.java(), Product.os());

    public Bdio2Document createBdio2Document(BdioMetadata bdioMetadata, Project project, DependencyGraph dependencyGraph) {
        Pair<List<Project>, List<Component>> subprojectsAndComponents = createAndLinkComponents(dependencyGraph, project);
        return new Bdio2Document(bdioMetadata, project, subprojectsAndComponents);
    }

    public BdioMetadata createBdioMetadata(String codeLocationName, ProjectInfo projectInfo, ZonedDateTime creationDateTime) {
        return createBdioMetadata(codeLocationName, projectInfo, creationDateTime, DEFAULT_PRODUCTS);
    }

    public BdioMetadata createBdioMetadata(String codeLocationName, ProjectInfo projectInfo, ZonedDateTime creationDateTime, Product product) {
        return createBdioMetadata(codeLocationName, projectInfo, creationDateTime, addLists(DEFAULT_PRODUCTS, Collections.singletonList(product)));
    }

    public BdioMetadata createBdioMetadata(String codeLocationName, ProjectInfo projectInfo, ZonedDateTime creationDateTime, List<Product> products) {
        ProductList productList =
            addLists(DEFAULT_PRODUCTS, products)
                .stream()
                .collect(ProductList.toProductList());

        return createBdioMetadata(codeLocationName, projectInfo, creationDateTime, productList);
    }

    public Project createProject(ExternalId projectExternalId, String projectName, String projectVersionName, boolean isRootProject) {
        Project project = new Project(projectExternalId.createBdioId().toString())
            .identifier(projectExternalId.createExternalId())
            .name(projectName)
            .version(projectVersionName);
        if (isRootProject) {
            project.namespace("root");
        }
        return project;
    }

    public Pair<List<Project>, List<Component>> createAndLinkComponents(DependencyGraph dependencyGraph, Project project) {
        return createAndLinkComponentsFromGraph(dependencyGraph, project::subproject, project::dependency, dependencyGraph.getRootDependencies(), new HashMap<>(), new HashMap<>());
    }

    private BdioMetadata createBdioMetadata(String codeLocationName, ProjectInfo projectInfo, ZonedDateTime creationDateTime, ProductList productList) {
        BdioMetadata metadata = new BdioMetadata()
            .id(LegacyUtilitiesClone.toNameUri(codeLocationName))
            .name(codeLocationName)
            .project(projectInfo.getNameVersion().getName())
            .project(projectInfo.getNameVersion().getVersion())
            .creationDateTime(creationDateTime)
            .publisher(productList);

        projectInfo.getProjectGroup().ifPresent(metadata::projectGroup);
        projectInfo.getCorrelationId().ifPresent(metadata::correlationId);

        projectInfo.getGitInfo().getSourceRevision().ifPresent(metadata::sourceRevision);
        projectInfo.getGitInfo().getSourceBranch().ifPresent(metadata::sourceBranch);
        projectInfo.getGitInfo().getSourceRepository()
            .map(URL::toString)
            .ifPresent(metadata::sourceRepository);

        return metadata;
    }

    private Pair<List<Project>, List<Component>> createAndLinkComponentsFromGraph(
        DependencyGraph dependencyGraph,
        @Nullable SubProjectFunction linkProjectDependency,
        DependencyFunction linkComponentDependency,
        Set<Dependency> dependencies,
        Map<ExternalId, Project> existingSubprojects,
        Map<ExternalId, Component> existingComponents
    ) {
        List<Project> addedSubprojects = new ArrayList<>();
        List<Component> addedComponents = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            if (dependency instanceof ProjectDependency) {
                if (linkProjectDependency == null) {
                    // Subprojects cannot be dependencies of components
                    // TODO is there a better way to handle this?
                    // passing subProjectFunction: component::dependency on line 124 might look better (but be more nonsensical?)
                    continue;
                }
                Project subproject = projectFromDependency(dependency);
                linkProjectDependency.subProject(new Project(subproject.id()).subproject(subproject));

                if (!existingSubprojects.containsKey(dependency.getExternalId())) {
                    addedSubprojects.add(subproject);
                    existingSubprojects.put(dependency.getExternalId(), subproject);
                    Pair<List<Project>, List<Component>> children = createAndLinkComponentsFromGraph(
                        dependencyGraph,
                        subproject::subproject,
                        subproject::dependency,
                        dependencyGraph.getChildrenForParent(dependency),
                        existingSubprojects,
                        existingComponents
                    );
                    addedSubprojects.addAll(children.getLeft());
                    addedComponents.addAll(children.getRight());
                }
            } else {
                Component component = componentFromDependency(dependency);
                linkComponentDependency.dependency(new com.blackducksoftware.bdio2.model.Dependency().dependsOn(component));

                if (!existingComponents.containsKey(dependency.getExternalId())) {
                    addedComponents.add(component);

                    existingComponents.put(dependency.getExternalId(), component);
                    Pair<List<Project>, List<Component>> children = createAndLinkComponentsFromGraph(
                        dependencyGraph,
                        null,
                        component::dependency,
                        dependencyGraph.getChildrenForParent(dependency),
                        existingSubprojects,
                        existingComponents
                    );
                    addedSubprojects.addAll(children.getLeft());
                    addedComponents.addAll(children.getRight());
                }
            }
        }
        return Pair.of(addedSubprojects, addedComponents);
    }

    private Project projectFromDependency(Dependency dependency) {
        return new Project(dependency.getExternalId().createBdioId().toString())
            .name(dependency.getName())
            .version(dependency.getVersion())
            .identifier(dependency.getExternalId().createExternalId())
            .namespace(dependency.getExternalId().getForge().getName());
    }

    private Component componentFromDependency(Dependency dependency) {
        return new Component(dependency.getExternalId().createBdioId().toString())
            .name(dependency.getName())
            .version(dependency.getVersion())
            .identifier(dependency.getExternalId().createExternalId())
            .namespace(dependency.getExternalId().getForge().getName());
    }

    private List<Product> addLists(List<Product> list1, List<Product> list2) {
        return Stream
            .concat(list1.stream(), list2.stream())
            .distinct()
            .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface DependencyFunction {
        BdioObject dependency(@Nullable com.blackducksoftware.bdio2.model.Dependency dependency);
    }

    @FunctionalInterface
    private interface SubProjectFunction {
        BdioObject subProject(@Nullable com.blackducksoftware.bdio2.model.Project subProject);
    }

}
