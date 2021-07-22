/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.apache.commons.lang3.tuple.Pair;

public class Bdio2Factory {
    public static final List<Product> DEFAULT_PRODUCTS = Arrays.asList(Product.java(), Product.os());

    public Bdio2Document createBdio2Document(final BdioMetadata bdioMetadata, final Project project, final DependencyGraph dependencyGraph) {
        final Pair<List<Project>, List<Component>> subprojectsAndComponents = createAndLinkComponents(dependencyGraph, project);
        return new Bdio2Document(bdioMetadata, project, subprojectsAndComponents);
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime) {
        return createBdioMetadata(codeLocationName, creationDateTime, DEFAULT_PRODUCTS);
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, Product product) {
        return createBdioMetadata(codeLocationName, creationDateTime, addLists(DEFAULT_PRODUCTS, Arrays.asList(product)));
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, List<Product> products) {
        ProductList productList =
            addLists(DEFAULT_PRODUCTS, products)
                .stream()
                .collect(ProductList.toProductList());

        return createBdioMetadata(codeLocationName, creationDateTime, productList);
    }

    @Deprecated
    /**
     * deprecated Please use the other createBdioMetadata methods that ask for lists of Products, a single Product, or no Product at all.
     */
    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, final ProductList.Builder productListBuilder) {
        DEFAULT_PRODUCTS
            .forEach(productListBuilder::addProduct);

        return createBdioMetadata(codeLocationName, creationDateTime, productListBuilder.build());
    }

    public Project createProject(final ExternalId projectExternalId, final String projectName, final String projectVersionName, boolean isRootProject) {
        Project project = new Project(projectExternalId.createBdioId().toString())
                   .identifier(projectExternalId.createExternalId())
                   .name(projectName)
                   .version(projectVersionName);
        if (isRootProject) {
            project.namespace("root");
        }
        return project;
    }

    public Pair<List<Project>, List<Component>> createAndLinkComponents(final DependencyGraph dependencyGraph, final Project project) {
        return createAndLinkComponentsFromGraph(dependencyGraph, project::subproject, project::dependency, dependencyGraph.getRootDependencies(), new HashMap<>(), new HashMap<>());
    }

    private BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, ProductList productList) {
        return new BdioMetadata()
                   .id(LegacyUtilitiesClone.toNameUri(codeLocationName))
                   .name(codeLocationName)
                   .creationDateTime(creationDateTime)
                   .publisher(productList);
    }

    private Pair<List<Project>, List<Component>> createAndLinkComponentsFromGraph(final DependencyGraph dependencyGraph, @Nullable final SubProjectFunction linkProjectDependency, final DependencyFunction linkComponentDependency, final Set<Dependency> dependencies, final Map<ExternalId, Project> existingSubprojects, final Map<ExternalId, Component> existingComponents) {
        final List<Project> addedSubprojects = new ArrayList<>();
        final List<Component> addedComponents = new ArrayList<>();

        for (final Dependency dependency : dependencies) {
            if (dependency instanceof ProjectDependency) {
                if (linkProjectDependency == null) {
                    // Subprojects cannot be dependencies of components
                    // TODO is there a better way to handle this?
                    // passing subProjectFunction: component::dependency on line 124 might look better (but be more nonsensical?)
                    continue;
                }
                final Project subproject = projectFromDependency(dependency);
                linkProjectDependency.subProject(new com.blackducksoftware.bdio2.model.Project(subproject.id()).subproject(subproject));

                if (!existingSubprojects.containsKey(dependency.getExternalId())) {
                    addedSubprojects.add(subproject);
                    existingSubprojects.put(dependency.getExternalId(), subproject);
                    final Pair<List<Project>, List<Component>> children = createAndLinkComponentsFromGraph(dependencyGraph, subproject::subproject, subproject::dependency, dependencyGraph.getChildrenForParent(dependency), existingSubprojects, existingComponents);
                    addedSubprojects.addAll(children.getLeft());
                    addedComponents.addAll(children.getRight());
                }
            } else {
                final Component component = componentFromDependency(dependency);
                linkComponentDependency.dependency(new com.blackducksoftware.bdio2.model.Dependency().dependsOn(component));

                if (!existingComponents.containsKey(dependency.getExternalId())) {
                    addedComponents.add(component);

                    existingComponents.put(dependency.getExternalId(), component);
                    final Pair<List<Project>, List<Component>> children = createAndLinkComponentsFromGraph(dependencyGraph, null, component::dependency, dependencyGraph.getChildrenForParent(dependency), existingSubprojects, existingComponents);
                    addedSubprojects.addAll(children.getLeft());
                    addedComponents.addAll(children.getRight());
                }
            }
        }
        return Pair.of(addedSubprojects, addedComponents);
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
