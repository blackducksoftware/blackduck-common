/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.model;

import java.util.List;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;
import org.apache.commons.lang3.tuple.Pair;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Project project;
    private final Pair<List<Project>, List<Component>> subprojectsAndComponents;

    public Bdio2Document(final BdioMetadata bdioMetadata, final Project project, final Pair<List<Project>, List<Component>> subprojectsAndComponents) {
        this.bdioMetadata = bdioMetadata;
        this.project = project;
        this.subprojectsAndComponents = subprojectsAndComponents;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Project getProject() {
        return project;
    }

    public Pair<List<Project>, List<Component>> getSubprojectsAndComponents() {
        return subprojectsAndComponents;
    }
}
