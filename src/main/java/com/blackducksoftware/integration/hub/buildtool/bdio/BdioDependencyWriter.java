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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.bdio.simple.BdioNodeFactory;
import com.blackducksoftware.integration.hub.bdio.simple.BdioPropertyHelper;
import com.blackducksoftware.integration.hub.bdio.simple.BdioWriter;
import com.blackducksoftware.integration.hub.bdio.simple.DependencyNodeTransformer;
import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode;
import com.blackducksoftware.integration.hub.bdio.simple.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.buildtool.BuildToolConstants;
import com.google.gson.Gson;

public class BdioDependencyWriter {
    private final Logger logger = LoggerFactory.getLogger(BdioDependencyWriter.class);

    private final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();

    private final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);

    private final DependencyNodeTransformer dependencyNodeTransformer = new DependencyNodeTransformer(bdioNodeFactory, bdioPropertyHelper);

    public static String getFilename(final String artifactId) {
        return artifactId + BuildToolConstants.BDIO_FILE_SUFFIX;
    }

    public void write(final File outputDirectory, final String hubCodeLocationName, final DependencyNode rootNode) throws IOException {
        // if the directory doesn't exist yet, let's create it
        outputDirectory.mkdirs();

        final String filename = getFilename(rootNode.name);
        final File file = new File(outputDirectory, filename);
        logger.info(String.format("Generating file: %s", file.getCanonicalPath()));

        try (final OutputStream outputStream = new FileOutputStream(file)) {
            writeProject(outputStream, hubCodeLocationName, rootNode);
        }
    }

    public void writeProject(final OutputStream outputStream, final String hubCodeLocationName, final DependencyNode root) throws IOException {
        final SimpleBdioDocument simpleBdioDocument = dependencyNodeTransformer.transformDependencyNode(hubCodeLocationName, root);

        try (BdioWriter bdioWriter = new BdioWriter(new Gson(), outputStream)) {
            bdioWriter.writeSimpleBdioDocument(simpleBdioDocument);
        }
    }

}
