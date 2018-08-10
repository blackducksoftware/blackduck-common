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
 * under the License.*/
package com.synopsys.integration.hub.dataservice.scan

import com.synopsys.integration.hub.api.generated.view.CodeLocationView
import com.synopsys.integration.hub.api.generated.view.ProjectView
import com.synopsys.integration.hub.rest.RestConnectionTestHelper
import com.synopsys.integration.hub.service.CodeLocationService
import com.synopsys.integration.hub.service.HubServicesFactory
import com.synopsys.integration.hub.service.ProjectService
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.test.annotation.IntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(IntegrationTest.class)
class ScanStatusDataServiceTestIT {
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    String uniqueProjectName = "hub-common-${System.currentTimeMillis()}"

    @After
    public void cleanup() {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService()
        CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName('hub-common/hub-common/27.0.0-SNAPSHOT gradle/bom')
        codeLocationService.deleteCodeLocation(codeLocationView)

        ProjectService projectDataService = hubServicesFactory.createProjectService()
        ProjectView project = projectDataService.getProjectByName(uniqueProjectName)
        projectDataService.deleteHubProject(project)
    }

    @Test
    void testBdioImportForNewProject() {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger

        // import the bdio
        final File file = restConnectionTestHelper.getFile('bdio/GRADLE_com_blackducksoftware_integration_hub_common_27_0_0_SNAPSHOT_hub_common_bdio.jsonld')
        String contents = file.text

        String version = '27.0.0-SNAPSHOT'
        String replacement = String.format('"name": "%s",', uniqueProjectName)
        String alteredContents = contents.replace('"name": "hub-common",', replacement)
        File uniquelyNamedBdio = File.createTempFile('uniquebdio', '.jsonld')
        uniquelyNamedBdio << alteredContents
        hubServicesFactory.createCodeLocationService().importBomFile(uniquelyNamedBdio);
        // wait for the scan to start/finish
        try {
            hubServicesFactory.createScanStatusService(FIVE_MINUTES).assertBomImportScanStartedThenFinished(uniqueProjectName, version);
        } catch (Exception e) {
            Assert.fail("Nothing should have been thrown: " + e.getMessage())
        }
    }

}
