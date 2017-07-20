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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.test.TestLogger;

public class ProjectRequestServiceTestIT {

    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final IntLogger logger = new TestLogger();

    private static HubServicesFactory hubServicesFactory;

    private static ProjectRequestService projectRequestService;

    final static String[] projectTestNames = {"ProjectRequestServiceTest1", "ProjectRequestServiceTest2",
    											"ProjectRequestServiceTest3", "ProjectRequestServiceTest4"};

    private static ProjectView[] activeProjects = new ProjectView[projectTestNames.length];

    private static String currName;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
      hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
      projectRequestService = hubServicesFactory.createProjectRequestService(logger);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
      for (ProjectView project : activeProjects){
        if (project != null){
          projectRequestService.deleteHubProject(project);
        }
      }
    }

    @Test
    public void testCreateAndDelete() throws IllegalArgumentException, IntegrationException {

    	//Test creation and getProjectByName
        try {
          for (int i = 0; i < projectTestNames.length; i++){
            currName = projectTestNames[i];
            projectRequestService.createHubProject(new ProjectRequest(currName));
            activeProjects[i] = projectRequestService.getProjectByName(currName);
          }
        } catch (com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException e){
          fail(currName + " already exists");
        } catch (com.blackducksoftware.integration.hub.exception.DoesNotExistException e){
          fail(currName + " should have been created but was not.");
        }

        //Test methods for getting project Matches
        try{
        	int numProjectsFound = projectRequestService.getAllProjectMatches("ProjectRequestServiceTest").size();
        	if (numProjectsFound != projectTestNames.length){
        		fail("getAllProjectMatches did not find the correct number of projects");
        	}

        	int limit = 2;
        	int numProjectsFoundLimit = projectRequestService.getProjectMatches("ProjectRequestServiceTest", limit).size();
        	assertEquals(numProjectsFoundLimit, limit);

        } catch (Exception e){
        	fail("Error when retrieving project matches");
        }

        //Test Deletion
        try {
          for (int i = 0; i < projectTestNames.length; i++){
            currName = projectTestNames[i];
            projectRequestService.deleteHubProject(activeProjects[i]);
            activeProjects[i] = null;
          }
        } catch (com.blackducksoftware.integration.hub.exception.DoesNotExistException e){
          fail("Error while deleting " + currName);
        }
    }

}
