/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.bdio.bdio2

import com.blackducksoftware.bdio2.BdioMetadata
import com.blackducksoftware.bdio2.model.Component
import com.blackducksoftware.bdio2.model.Project
import com.blackducksoftware.common.value.Product
import com.blackducksoftware.common.value.ProductList
import com.synopsys.integration.bdio.graph.DependencyGraph
import com.synopsys.integration.bdio.model.dependency.Dependency
import com.synopsys.integration.bdio.model.externalid.ExternalId
import com.synopsys.integration.blackduck.bdio2.LegacyUtilitiesClone
import java.time.ZonedDateTime

class Bdio2Factory {

    fun createBdio2Document(bdioMetadata: BdioMetadata, project: Project, dependencyGraph: DependencyGraph): Bdio2Document {
        val components: List<Component> = createAndLinkComponents(dependencyGraph, project)
        return Bdio2Document(bdioMetadata, project, components)
    }

    fun createBdioMetadata(codeLocationName: String, creationDateTime: ZonedDateTime = ZonedDateTime.now(), productListBuilder: ProductList.Builder = ProductList.Builder()): BdioMetadata {
        return BdioMetadata()
                .id(LegacyUtilitiesClone.toNameUri(codeLocationName))
                .name(codeLocationName)
                .creationDateTime(creationDateTime)
                .publisher(productListBuilder
                        .addProduct(Product.java())
                        .addProduct(Product.os())
                        .build()
                )
    }

    fun createProject(projectExternalId: ExternalId, projectName: String? = projectExternalId.name, projectVersionName: String? = projectExternalId.version): Project {
        return Project(projectExternalId.createBdioId().toString())
                .identifier(projectExternalId.createExternalId())
                .name(projectName)
                .version(projectVersionName)
    }


    /**
     * Converts a DependencyGraph to a List<Component> and adds the appropriate relationships to Project.
     */
    fun createAndLinkComponents(dependencyGraph: DependencyGraph, project: Project): List<Component> {
        return createAndLinkComponentsFromGraph(dependencyGraph, project, dependencyGraph.rootDependencies)
    }

    private fun createAndLinkComponentsFromGraph(dependencyGraph: DependencyGraph, project: Project, dependencies: Set<Dependency>): List<Component> {
        val existingComponents = mutableMapOf<ExternalId, Component>()
        val addedComponents = mutableListOf<Component>()

        for (dependency in dependencies) {
            val component = componentFromDependency(dependency)
            project.dependency(com.blackducksoftware.bdio2.model.Dependency().dependsOn(component))

            if (!existingComponents.containsKey(dependency.externalId)) {
                addedComponents.add(component)

                existingComponents[dependency.externalId] = component
                val children = createAndLinkComponentsToParent(dependencyGraph, component, dependencyGraph.getChildrenForParent(dependency), existingComponents)
                addedComponents.addAll(children)
            }
        }

        return addedComponents
    }

    private fun createAndLinkComponentsToParent(graph: DependencyGraph, currentComponent: Component, dependencies: Set<Dependency>, existingComponents: MutableMap<ExternalId, Component>): List<Component> {
        val addedComponents = mutableListOf<Component>()

        for (dependency in dependencies) {
            val component = componentFromDependency(dependency)
            currentComponent.dependency(com.blackducksoftware.bdio2.model.Dependency().dependsOn(component))

            if (!existingComponents.containsKey(dependency.externalId)) {
                addedComponents.add(component)

                existingComponents[dependency.externalId] = component
                val children = createAndLinkComponentsToParent(graph, component, graph.getChildrenForParent(dependency), existingComponents)
                addedComponents.addAll(children)
            }
        }

        return addedComponents
    }

    private fun componentFromDependency(dependency: Dependency): Component {
        val component = Component(dependency.externalId.createBdioId().toString())
        component.name(dependency.name)
        component.version(dependency.version)
        component.identifier(dependency.externalId.createExternalId())
        component.namespace(dependency.externalId.forge.name)

        return component
    }

}
