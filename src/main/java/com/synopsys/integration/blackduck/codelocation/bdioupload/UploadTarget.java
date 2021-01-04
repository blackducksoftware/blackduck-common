/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.NameVersion;

public class UploadTarget {
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final File uploadFile;
    private final String mediaType;

    public static UploadTarget createDefault(NameVersion projectAndVersion, String codeLocationName, File uploadFile) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, "application/ld+json");
    }

    public static UploadTarget createWithMediaType(NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, mediaType);
    }

    private UploadTarget(NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) throws IllegalArgumentException {
        if (StringUtils.isAnyBlank(projectAndVersion.getName(), projectAndVersion.getVersion())) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank project and version.");
        }
        if (StringUtils.isBlank(codeLocationName)) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank codeLocationName.");
        }

        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.uploadFile = uploadFile;
        this.mediaType = mediaType;
    }

    public NameVersion getProjectAndVersion() {
        return projectAndVersion;
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public String getMediaType() {
        return mediaType;
    }

}
