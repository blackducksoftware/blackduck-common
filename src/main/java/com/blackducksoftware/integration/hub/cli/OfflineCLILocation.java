/**
 * hub-common
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
package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.IOException;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.IntLogger;

public class OfflineCLILocation extends CLILocation {
    public OfflineCLILocation(final IntLogger logger, final File cliUnzippedDirectory) {
        super(logger, cliUnzippedDirectory);
    }

    @Override
    public File getCLIInstallDir() {
        return getDirectoryToInstallTo();
    }

    @Override
    public File getCLIHome() throws IOException {
        return getDirectoryToInstallTo();
    }

    @Override
    public File createHubVersionFile() throws HubIntegrationException, IOException {
        return null;
    }

    @Override
    public String getCLIDownloadUrl(final IntLogger logger, final String hubUrl) {
        return null;
    }

}
