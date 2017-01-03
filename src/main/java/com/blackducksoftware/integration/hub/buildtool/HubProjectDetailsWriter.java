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
package com.blackducksoftware.integration.hub.buildtool;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubProjectDetailsWriter {
    private final Logger logger = LoggerFactory.getLogger(HubProjectDetailsWriter.class);

    public void write(final File outputDirectory, final String hubProjectName, final String hubProjectVersionName) throws IOException {
        // if the directory doesn't exist yet, let's create it
        outputDirectory.mkdirs();

        createFile(outputDirectory, BuildToolConstants.HUB_PROJECT_NAME_FILE_NAME, hubProjectName);
        createFile(outputDirectory, BuildToolConstants.HUB_PROJECT_VERSION_NAME_FILE_NAME, hubProjectVersionName);
    }

    private void createFile(final File outputDirectory, final String filename, final String contents) throws IOException {
        final File file = new File(outputDirectory, filename);
        logger.info(String.format("Generating file: %s", file.getCanonicalPath()));

        FileUtils.writeStringToFile(file, contents, "UTF8");
    }

}
