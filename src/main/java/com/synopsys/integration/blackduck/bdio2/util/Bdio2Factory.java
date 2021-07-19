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
import java.util.HashSet;
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
        final Bdio2DocumentBuilder documentBuilder = new Bdio2DocumentBuilder();
        createAndLinkComponentsFromGraph(dependencyGraph, project::subproject, project::dependency, dependencyGraph.getRootDependencies(), documentBuilder);
        return new Bdio2Document(bdioMetadata, project, documentBuilder.getEncounteredProjects(), documentBuilder.getEncounteredComponents());
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

    private BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, ProductList productList) {
        return new BdioMetadata()
                   .id(LegacyUtilitiesClone.toNameUri(codeLocationName))
                   .name(codeLocationName)
                   .creationDateTime(creationDateTime)
                   .publisher(productList);
    }

    private void createAndLinkComponentsFromGraph(final DependencyGraph dependencyGraph, @Nullable final SubProjectFunction subProjectFunction, final DependencyFunction dependentComponentFunction, final Set<Dependency> dependencies, Bdio2DocumentBuilder documentBuilder) {
        for (final Dependency dependency : dependencies) {
            if (dependency instanceof ProjectDependency) {
                if (subProjectFunction == null) {
                    // Subprojects cannot be dependencies of components
                    // TODO is there a better way to handle this?
                    // passing subProjectFunction: component::dependency on line 124 might look better (but be more nonsensical?)
                    continue;
                }
                final Project subproject = documentBuilder.getOrComputeProjectIfAbsent(dependency,
                    (newsubproject) -> createAndLinkComponentsFromGraph(dependencyGraph, newsubproject::subproject, newsubproject::dependency, dependencyGraph.getChildrenForParent(dependency), documentBuilder));

                subProjectFunction.subProject(new Project(subproject.id()).subproject(subproject));
            } else {
                final Component component = documentBuilder.getOrComputeComponentIfAbsent(dependency,
                    (newcomponent) -> createAndLinkComponentsFromGraph(dependencyGraph, null, newcomponent::dependency, dependencyGraph.getChildrenForParent(dependency), documentBuilder));

                dependentComponentFunction.dependency(new com.blackducksoftware.bdio2.model.Dependency().dependsOn(component));
            }
        }
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
