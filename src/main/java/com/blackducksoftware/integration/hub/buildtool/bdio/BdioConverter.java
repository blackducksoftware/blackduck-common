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

import com.blackducksoftware.integration.hub.bdio.simple.BdioComponent;
import com.blackducksoftware.integration.hub.bdio.simple.BdioExternalIdentifier;
import com.blackducksoftware.integration.hub.bdio.simple.BdioNode;
import com.blackducksoftware.integration.hub.bdio.simple.BdioProject;
import com.blackducksoftware.integration.hub.bdio.simple.BdioRelationship;
import com.blackducksoftware.integration.hub.buildtool.DependencyNode;
import com.blackducksoftware.integration.hub.buildtool.Gav;

public class BdioConverter {
    public BdioProject createProject(final Gav gav, final String projectName, final List<DependencyNode> children) {
        final String id = createIdForMavenArtifact(gav);
        final BdioExternalIdentifier externalIdentifier = createExternalIdentifierForMavenArtifact(gav);

        final BdioProject project = new BdioProject();
        project.setId(id);
        project.setName(projectName);
        project.setRevision(gav.getVersion());
        project.setBdioExternalIdentifier(externalIdentifier);
        addRelationships(project, children);

        return project;
    }

    public BdioComponent createComponent(final Gav gav, final List<DependencyNode> children) {
        final String id = createIdForMavenArtifact(gav);
        final BdioExternalIdentifier externalIdentifier = createExternalIdentifierForMavenArtifact(gav);

        final BdioComponent component = new BdioComponent();
        component.setId(id);
        component.setRevision(gav.getVersion());
        component.setBdioExternalIdentifier(externalIdentifier);
        addRelationships(component, children);

        return component;
    }

    private void addRelationships(final BdioNode node, final List<DependencyNode> children) {
        for (final DependencyNode child : children) {
            final Gav childGav = child.getGav();
            final BdioRelationship singleRelationship = new BdioRelationship();
            singleRelationship.setRelated(createIdForMavenArtifact(childGav));
            singleRelationship.setRelationshipType("DYNAMIC_LINK");
            node.addRelationship(singleRelationship);
        }
    }

    private String createIdForMavenArtifact(final Gav gav) {
        return String.format("mvn:%s/%s/%s", gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

    private BdioExternalIdentifier createExternalIdentifierForMavenArtifact(final Gav gav) {
        final BdioExternalIdentifier externalIdentifier = new BdioExternalIdentifier();
        externalIdentifier.setExternalId(gav.toString());
        externalIdentifier.setExternalSystemTypeId("maven");
        return externalIdentifier;
    }

}
