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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.bdio.simple.BdioNodeFactory;
import com.blackducksoftware.integration.hub.bdio.simple.BdioPropertyHelper;
import com.blackducksoftware.integration.hub.bdio.simple.BdioWriter;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioBillOfMaterials;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioComponent;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioExternalIdentifier;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioProject;
import com.blackducksoftware.integration.hub.buildtool.BuildToolConstants;
import com.blackducksoftware.integration.hub.buildtool.DependencyNode;
import com.blackducksoftware.integration.hub.buildtool.Gav;
import com.google.gson.Gson;

public class BdioDependencyWriter {
    private final Logger logger = LoggerFactory.getLogger(BdioDependencyWriter.class);

    private final Set<String> externalIds = new HashSet<>();

    BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();

    BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);

    public static String getFilename(final String artifactId) {
        return artifactId + BuildToolConstants.BDIO_FILE_SUFFIX;
    }

    public void write(final File outputDirectory, final String artifactId, final String hubProjectName,
            final DependencyNode rootNode) throws IOException {
        // if the directory doesn't exist yet, let's create it
        outputDirectory.mkdirs();

        final String filename = getFilename(artifactId);
        final File file = new File(outputDirectory, filename);
        logger.info(String.format("Generating file: %s", file.getCanonicalPath()));

        try (final OutputStream outputStream = new FileOutputStream(file)) {
            writeProject(outputStream, hubProjectName, rootNode);
        }
    }

    public void writeProject(final OutputStream outputStream, final String projectName, final DependencyNode root)
            throws IOException {
        final BdioBillOfMaterials billOfMaterials = bdioNodeFactory.createBillOfMaterials(projectName);

        final String projectVersion = root.getGav().getVersion();
        final String projectId = idFromGav(root.getGav());
        final BdioExternalIdentifier projectExternalIdentifier = externalIdentifierFromGav(root.getGav());
        final BdioProject project = bdioNodeFactory.createProject(projectName, projectVersion, projectId, projectExternalIdentifier);

        for (final DependencyNode child : root.getChildren()) {
            final BdioComponent component = componentFromDependencyNode(child);
            bdioPropertyHelper.addRelationship(project, component);
        }

        try (BdioWriter bdioWriter = new BdioWriter(new Gson(), outputStream)) {
            bdioWriter.writeBdioNode(billOfMaterials);
            bdioWriter.writeBdioNode(project);

            for (final DependencyNode child : root.getChildren()) {
                writeDependencyGraph(bdioWriter, child);
            }
        }
    }

    private void writeDependencyGraph(final BdioWriter writer, final DependencyNode dependencyNode) throws IOException {
        writeDependencyNode(writer, dependencyNode);

        for (final DependencyNode child : dependencyNode.getChildren()) {
            writeDependencyGraph(writer, child);
        }
    }

    private void writeDependencyNode(final BdioWriter writer, final DependencyNode dependencyNode) throws IOException {
        final BdioComponent bdioComponent = componentFromDependencyNode(dependencyNode);
        final BdioExternalIdentifier externalIdentifier = bdioComponent.bdioExternalIdentifier;
        boolean alreadyAdded = false;
        if (!externalIds.add(externalIdentifier.externalId)) {
            alreadyAdded = true;
        }

        if (!alreadyAdded) {
            for (final DependencyNode child : dependencyNode.getChildren()) {
                final BdioComponent childComponent = componentFromDependencyNode(child);
                bdioPropertyHelper.addRelationship(bdioComponent, childComponent);
            }
            writer.writeBdioNode(bdioComponent);
        }
    }

    private BdioComponent componentFromDependencyNode(final DependencyNode dependencyNode) {
        final String componentName = dependencyNode.getGav().getArtifactId();
        final String componentVersion = dependencyNode.getGav().getVersion();
        final String componentId = idFromGav(dependencyNode.getGav());
        final BdioExternalIdentifier componentExternalIdentifier = externalIdentifierFromGav(dependencyNode.getGav());

        final BdioComponent component = bdioNodeFactory.createComponent(componentName, componentVersion, componentId, componentExternalIdentifier);
        return component;
    }

    private BdioExternalIdentifier externalIdentifierFromGav(final Gav gav) {
        final String group = gav.getGroupId();
        final String artifact = gav.getArtifactId();
        final String version = gav.getVersion();

        final BdioExternalIdentifier externalIdentifier = bdioPropertyHelper.createMavenExternalIdentifier(group, artifact, version);
        return externalIdentifier;
    }

    private String idFromGav(final Gav gav) {
        final String group = gav.getGroupId();
        final String artifact = gav.getArtifactId();
        final String version = gav.getVersion();

        final String id = bdioPropertyHelper.createBdioId(group, artifact, version);
        return id;
    }
}
