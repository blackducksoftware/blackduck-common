/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.buildtool.bdio;

import java.util.List;

import com.blackducksoftware.bdio.model.Component;
import com.blackducksoftware.bdio.model.ExternalIdentifier;
import com.blackducksoftware.bdio.model.Project;
import com.blackducksoftware.bdio.model.Relationship;
import com.blackducksoftware.integration.hub.buildtool.DependencyNode;
import com.blackducksoftware.integration.hub.buildtool.Gav;

public class BdioConverter {
    private final BdioIdCreator bdioIdCreator = new BdioIdCreator();

    public Project createProject(final Gav gav, final String projectName, final List<DependencyNode> children) {
        final String id = bdioIdCreator.createMavenId(gav);
        final ExternalIdentifier externalIdentifier = bdioIdCreator.createExternalIdentifier(gav);

        final Project project = new Project();
        project.setId(id);
        project.setName(projectName);
        project.setVersion(gav.getVersion());
        project.addExternalIdentifier(externalIdentifier);
        addRelationships(project, children);

        return project;
    }

    public Component createComponent(final Gav gav, final List<DependencyNode> children) {
        final String id = bdioIdCreator.createMavenId(gav);
        final ExternalIdentifier externalIdentifier = bdioIdCreator.createExternalIdentifier(gav);

        final Component component = new Component();
        component.setId(id);
        component.setVersion(gav.getVersion());
        component.addExternalIdentifier(externalIdentifier);
        addRelationships(component, children);

        return component;
    }

    private void addRelationships(final Project project, final List<DependencyNode> children) {
        for (final DependencyNode child : children) {
            final Gav childGav = child.getGav();
            project.addRelationship(Relationship.dynamicLink(bdioIdCreator.createMavenId(childGav)));
        }
    }

    private void addRelationships(final Component component, final List<DependencyNode> children) {
        for (final DependencyNode child : children) {
            final Gav childGav = child.getGav();
            component.addRelationship(Relationship.dynamicLink(bdioIdCreator.createMavenId(childGav)));
        }
    }

}
