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
package com.blackducksoftware.integration.hub.api.report;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class RiskReportResourceCopier extends JarResourceCopier {
    public final static String JSON_TOKEN_TO_REPLACE = "TOKEN_RISK_REPORT_JSON_TOKEN";

    public final static String RESOURCE_DIRECTORY = "riskreport/web/";

    public final static String RISK_REPORT_HTML_FILE_NAME = "riskreport.html";

    private final String destinationDirectory;

    public RiskReportResourceCopier(final String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public List<File> copy() throws IOException, URISyntaxException {
        return copy(RESOURCE_DIRECTORY, destinationDirectory);
    }

    @Override
    public List<String> findRelativePathFileList() {
        final List<String> relativePathList = new LinkedList<>();
        relativePathList.add("css/HubBomReport.css");
        relativePathList.add("images/Hub_BD_logo.png");
        relativePathList.add(RISK_REPORT_HTML_FILE_NAME);
        relativePathList.addAll(findJavascriptFileList());
        // relativePathList.addAll(findFontAwesomeFileList());
        return relativePathList;
    }

    private List<String> findJavascriptFileList() {
        final List<String> fileList = new LinkedList<>();
        final String parentDir = "js/";
        fileList.add(parentDir + "HubBomReportFunctions.js");
        fileList.add(parentDir + "HubRiskReport.js");
        fileList.add(parentDir + "jquery-3.1.1.min.js");
        fileList.add(parentDir + "Sortable.js");
        return fileList;
    }

}
