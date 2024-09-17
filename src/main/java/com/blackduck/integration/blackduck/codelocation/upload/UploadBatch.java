/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.upload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadBatch {
    private final List<UploadTarget> uploadTargets = new ArrayList<>();

    public UploadBatch() {
    }

    public UploadBatch(UploadTarget... uploadTargets) {
        this.uploadTargets.addAll(Arrays.asList(uploadTargets));
    }

    public void addUploadTarget(final UploadTarget uploadTarget) {
        uploadTargets.add(uploadTarget);
    }

    public List<UploadTarget> getUploadTargets() {
        return uploadTargets;
    }

}
