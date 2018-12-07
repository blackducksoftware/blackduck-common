/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.service.model.pdf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.exception.RiskReportException;
import com.synopsys.integration.blackduck.service.model.ReportData;

public class RiskReportWriter {
    public void createHtmlReportFiles(final Gson gson, final File outputDirectory, final ReportData reportData) throws RiskReportException {
        try {
            final RiskReportResourceCopier copier = new RiskReportResourceCopier(outputDirectory.getCanonicalPath());
            File htmlFile = null;
            try {
                final List<File> writtenFiles = copier.copy();
                for (final File file : writtenFiles) {
                    if (file.getName().equals(RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME)) {
                        htmlFile = file;
                        break;
                    }
                }
            } catch (final URISyntaxException e) {
                throw new RiskReportException("Couldn't post the report: " + e.getMessage(), e);
            }
            if (htmlFile == null) {
                throw new RiskReportException("Could not find the file : " + RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME
                                                      + ", the report files must not have been copied into the report directory.");
            }
            String htmlFileString = FileUtils.readFileToString(htmlFile, "UTF-8");
            final String reportString = gson.toJson(reportData);
            htmlFileString = htmlFileString.replace(RiskReportResourceCopier.JSON_TOKEN_TO_REPLACE, reportString);
            FileUtils.writeStringToFile(htmlFile, htmlFileString, "UTF-8");
        } catch (final IOException e) {
            throw new RiskReportException("Couldn't post the report: " + e.getMessage(), e);
        }
    }

}
