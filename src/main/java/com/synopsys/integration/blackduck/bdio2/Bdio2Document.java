/*
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
package com.synopsys.integration.blackduck.bdio2;

import java.util.List;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Project project;
    private final List<Component> components;

    public Bdio2Document(final BdioMetadata bdioMetadata, final Project project, final List<Component> components) {
        this.bdioMetadata = bdioMetadata;
        this.project = project;
        this.components = components;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Project getProject() {
        return project;
    }

    public List<Component> getComponents() {
        return components;
    }
}
