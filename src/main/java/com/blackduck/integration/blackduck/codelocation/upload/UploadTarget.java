/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.upload;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.util.NameVersion;

public class UploadTarget {
    @Nullable
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final File uploadFile;
    private final String mediaType;

    private UploadTarget(@Nullable NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) throws IllegalArgumentException {
        if (StringUtils.isBlank(codeLocationName)) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank codeLocationName.");
        }

        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.uploadFile = uploadFile;
        this.mediaType = mediaType;
    }

    public static UploadTarget createDefault(String codeLocationName, File uploadFile) {
        return createDefault(null, codeLocationName, uploadFile);
    }

    public static UploadTarget createDefault(@Nullable NameVersion projectAndVersion, String codeLocationName, File uploadFile) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, "application/ld+json");
    }

    public static UploadTarget createWithMediaType(String codeLocationName, File uploadFile, String mediaType) {
        return createWithMediaType(null, codeLocationName, uploadFile, mediaType);
    }

    public static UploadTarget createWithMediaType(@Nullable NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, mediaType);
    }

    public Optional<NameVersion> getProjectAndVersion() {
        return Optional.ofNullable(projectAndVersion);
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
