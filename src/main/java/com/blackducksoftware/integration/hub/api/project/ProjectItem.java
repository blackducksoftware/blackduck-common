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
package com.blackducksoftware.integration.hub.api.project;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.model.type.ProjectSourceEnum;

public class ProjectItem extends HubItem {
    // description from Hub API: "The general identifier of the project"
    private String name;

    // description from Hub API: "Summary of what the project represents in terms of functionality and use"
    private String description;

    // description from Hub API: "Whether BOM level adjustments are applied at the project level (to all releases)"
    private boolean projectLevelAdjustments;

    // description from Hub API: "Allowed values : [1,2,3,4,5]"
    private int projectTier;

    private ProjectSourceEnum source;

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

    public ProjectSourceEnum getSource() {
        return source;
    }

}
