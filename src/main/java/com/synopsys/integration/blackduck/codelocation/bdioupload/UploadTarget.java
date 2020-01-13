/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

public class UploadTarget {
    private final String codeLocationName;
    private final File uploadFile;
    private final String mediaType;

    public static UploadTarget createDefault(final String codeLocationName, final File uploadFile) {
        return new UploadTarget(codeLocationName, uploadFile, "application/ld+json");
    }

    public static UploadTarget createWithMediaType(final String codeLocationName, final File uploadFile, final String mediaType) {
        return new UploadTarget(codeLocationName, uploadFile, mediaType);
    }

    private UploadTarget(final String codeLocationName, final File uploadFile, final String mediaType) throws IllegalArgumentException {
        if (StringUtils.isBlank(codeLocationName)) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank codeLocationName.");
        }

        this.codeLocationName = codeLocationName;
        this.uploadFile = uploadFile;
        this.mediaType = mediaType;
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
