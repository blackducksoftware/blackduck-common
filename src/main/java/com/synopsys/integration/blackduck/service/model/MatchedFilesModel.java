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
package com.synopsys.integration.blackduck.service.model;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.component.ComponentMatchedFilesItemsFilePathView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;

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
