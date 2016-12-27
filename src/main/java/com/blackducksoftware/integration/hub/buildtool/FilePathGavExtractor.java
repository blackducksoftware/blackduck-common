/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc.
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
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class FilePathGavExtractor {
    public Gav getMavenPathGav(final String filePath, final String localMavenRepoPath) {
        if (filePath == null || localMavenRepoPath == null) {
            return null;
        }

        final String cleanedFilePath = filePath.replaceFirst(localMavenRepoPath, "");
        final String[] cleanedFilePathSegments = cleanedFilePath.split(File.separator);

        String[] groupIdSegments;
        if (cleanedFilePathSegments[0].equals("")) {
            if (cleanedFilePathSegments.length < 4) {
                return null;
            }
            groupIdSegments = Arrays.copyOfRange(cleanedFilePathSegments, 1, cleanedFilePathSegments.length - 3);
        } else {
            if (cleanedFilePathSegments.length < 3) {
                return null;
            }
            groupIdSegments = Arrays.copyOfRange(cleanedFilePathSegments, 0, cleanedFilePathSegments.length - 3);
        }

        final String groupId = StringUtils.join(groupIdSegments, ".");
        final String artifactId = cleanedFilePathSegments[cleanedFilePathSegments.length - 3];
        final String version = cleanedFilePathSegments[cleanedFilePathSegments.length - 2];

        return new Gav(groupId, artifactId, version);

    }

    public Gav getGradlePathGav(final String filePath) {
        if (filePath == null) {
            return null;
        }

        final String[] filePathSegments = filePath.split(File.separator);

        if (filePathSegments.length < 5) {
            return null;
        }

        final String groupId = filePathSegments[filePathSegments.length - 5];
        final String artifactId = filePathSegments[filePathSegments.length - 4];
        final String version = filePathSegments[filePathSegments.length - 3];

        return new Gav(groupId, artifactId, version);
    }

}
