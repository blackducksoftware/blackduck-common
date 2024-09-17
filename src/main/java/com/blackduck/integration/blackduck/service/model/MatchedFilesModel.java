/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import com.blackduck.integration.blackduck.api.generated.component.ComponentMatchedFilesItemsFilePathView;
import com.blackduck.integration.blackduck.api.generated.enumeration.UsageType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentMatchedFilesView;

import java.util.HashSet;
import java.util.Set;

public class MatchedFilesModel {
    private final String path;
    private final String archiveContext;
    private final String fileName;
    private final String compositePathContext;
    private final Set<UsageType> usages;

    public MatchedFilesModel(final ComponentMatchedFilesView matchedFile) {
        final ComponentMatchedFilesItemsFilePathView pathView = matchedFile.getFilePath();
        path = pathView.getPath();
        archiveContext = pathView.getArchiveContext();
        fileName = pathView.getFileName();
        compositePathContext = pathView.getCompositePathContext();
        usages = new HashSet<UsageType>(matchedFile.getUsages());
    }

    public String getPath() {
        return path;
    }

    public String getArchiveContext() {
        return archiveContext;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCompositePathContext() {
        return compositePathContext;
    }

    public Set<UsageType> getUsages() {
        return usages;
    }
}
