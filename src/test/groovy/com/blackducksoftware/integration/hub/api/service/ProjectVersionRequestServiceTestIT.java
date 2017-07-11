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
package com.blackducksoftware.integration.hub.api.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.test.TestLogger;

public class ProjectVersionRequestServiceTestIT {

    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final IntLogger logger = new TestLogger();

    private static HubServicesFactory hubServicesFactory;

    private static ProjectRequestService projectRequestService;

    private static ProjectVersionRequestService projectVersionRequestService;

    private static String TestName1 = "ProjectVersionServiceTest1";

    private static String TestName2 = "ProjectVersionServiceTest2";

    private static String testProjectVersion1 = "1";

    private static String testProjectVersion2 = "2";

    private static ProjectView project1;

    private static ProjectView project2;

    private static String TestURL;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
      hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
      projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService(logger);
      projectRequestService = hubServicesFactory.createProjectRequestService(logger);

      TestURL = projectRequestService.createHubProject(new ProjectRequest(TestName2));
      project2 = projectRequestService.getProjectByName(TestName2);

      projectRequestService.createHubProject(new ProjectRequest(TestName1));
      project1 = projectRequestService.getProjectByName(TestName1);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	if (project1 != null){
    		projectRequestService.deleteHubProject(project1);
    	}
    	if (project2 != null){
    		projectRequestService.deleteHubProject(project2);
    	}
    }

    @Test
    public void test() throws IllegalArgumentException, IntegrationException {


      	try{
      		projectVersionRequestService.createHubVersion(project1,
          		new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersion1));
      	} catch (Exception e){
      		fail("error when creating hub version");
      	}

      	final ProjectVersionView projectVersion = projectVersionRequestService.getProjectVersion(project1, testProjectVersion1);
      	assertEquals(testProjectVersion1, projectVersion.versionName);

      	final List<ProjectVersionView> projectVersionAll = projectVersionRequestService.getAllProjectVersions(project1);
      	assertNotNull(projectVersionAll);
      	System.out.println(projectVersionAll.size());
      	System.out.println(projectVersionRequestService.getAllProjectVersions(project1));

        projectRequestService.deleteHubProject(projectRequestService.getProjectByName(TestName1));
        project1 = null;
    }

    @Test
    public void testWithURL() throws IllegalArgumentException, IntegrationException {

      ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersion2);
  		assertNotNull(projectVersionRequest);

  		final MetaService metaService = hubServicesFactory.createMetaService(logger);
  		final String versionURL = metaService.getFirstLink(project2, MetaService.VERSIONS_LINK);

  		try{
  			projectVersionRequestService.createHubVersion(versionURL, projectVersionRequest);
  		}
  		catch (Exception e){
  			fail("Error creating Hub Version with versionURL");
  		}

    	final List<ProjectVersionView> projectVersionAll = projectVersionRequestService.getAllProjectVersions(versionURL);
    	assertNotNull(projectVersionAll);
    	System.out.println("Num Project Versions:" + projectVersionAll.size());

    	projectRequestService.deleteHubProject(projectRequestService.getProjectByName(TestName2));
    	project2 = null;
    }

}
