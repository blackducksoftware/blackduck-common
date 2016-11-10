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
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blackducksoftware.integration.hub.api.report.ArtifactPublisher;

public class ArtifactPublisherTest {

    private static final String RISK_REPORT_DIR = "risk_report_dir";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPublish() throws Exception {
        folder.create();
        final File dirToWriteTo = folder.newFolder();
        final File riskReportDir = new File(dirToWriteTo, RISK_REPORT_DIR);
        ArtifactPublisher artifactPublisher = new ArtifactPublisher(riskReportDir);
        List<File> writtenFiles = artifactPublisher.publish();
        for (File file : writtenFiles) {
            System.out.println(file.getCanonicalPath());
        }

        assertFalse(writtenFiles.isEmpty());
    }
}
