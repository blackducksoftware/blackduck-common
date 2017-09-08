/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model;

import java.util.Set;

import com.blackducksoftware.integration.hub.model.enumeration.MatchedFileUsageEnum;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.components.FilePathView;

public class MatchedFilesModel {
    private final String path;
    private final String archiveContext;
    private final String fileName;
    private final String compositePathContext;
    private final Set<MatchedFileUsageEnum> usages;

    public MatchedFilesModel(final MatchedFilesView matchedFile) {
        final FilePathView pathView = matchedFile.filePath;
        this.path = pathView.path;
        this.archiveContext = pathView.archiveContext;
        this.fileName = pathView.fileName;
        this.compositePathContext = pathView.compositePathContext;
        this.usages = matchedFile.usages;
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

    public Set<MatchedFileUsageEnum> getUsages() {
        return usages;
    }
}
