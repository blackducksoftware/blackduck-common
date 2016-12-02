/*******************************************************************************
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.project;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.project.version.SourceEnum;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ProjectItem extends HubItem {
    public static final String PROJECT_URL_IDENTIFIER = "projects";

    public static final String VERSION_LINK = "versions";

    public static final String CANONICAL_VERSION_LINK = "canonicalVersion";

    // description from Hub API: "The general identifier of the project"
    private final String name;

    // description from Hub API: "Summary of what the project represents in terms of functionality and use"
    private final String description;

    // description from Hub API: "Whether BOM level adjustments are applied at the project level (to all releases)"
    private final boolean projectLevelAdjustments;

    // description from Hub API: "Allowed values : [1,2,3,4,5]"
    private final int projectTier;

    private final SourceEnum source;

    public ProjectItem(MetaInformation meta, String name, String description, boolean projectLevelAdjustments, int projectTier, SourceEnum source) {
        super(meta);
        this.name = name;
        this.description = description;
        this.projectLevelAdjustments = projectLevelAdjustments;
        this.projectTier = projectTier;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isProjectLevelAdjustments() {
        return projectLevelAdjustments;
    }

    public int getProjectTier() {
        return projectTier;
    }

    public SourceEnum getSource() {
        return source;
    }

}
