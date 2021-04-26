/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.model;

import com.blackducksoftware.bdio2.BdioMetadata;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Bdio2Project bdio2Project;

    public Bdio2Document(final BdioMetadata bdioMetadata, Bdio2Project bdio2Project) {
        this.bdioMetadata = bdioMetadata;
        this.bdio2Project = bdio2Project;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Bdio2Project getProject() {
        return bdio2Project;
    }
}
