/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api.report;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class RiskReportResourceCopier {

    public final static String RESOURCE_DIRECTORY = "riskreport/web";

    private JarResourceCopier resourceCopier = new JarResourceCopier();

    private String destinationDirectory;

    public RiskReportResourceCopier(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public List<File> copy() throws IOException, URISyntaxException {
        return resourceCopier.copy(RESOURCE_DIRECTORY, destinationDirectory);
    }
}
