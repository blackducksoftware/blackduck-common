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
package com.synopsys.integration.blackduck.request.validator

import static org.junit.jupiter.api.Assertions.*;

import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder
import com.synopsys.integration.rest.RestConstants
import org.junit.jupiter.api.Test

import java.text.SimpleDateFormat

class ProjectRequestValidatorTest {
    @Test
    public void testInvalidValues() {
        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(null)
        projectRequestBuilder.setProjectLevelAdjustments(null)
        projectRequestBuilder.setProjectOwner(null)
        projectRequestBuilder.setProjectTier(null)
        projectRequestBuilder.setDescription(null)
        projectRequestBuilder.setVersionName(null)
        projectRequestBuilder.setDistribution(null)
        projectRequestBuilder.setPhase(null)
        projectRequestBuilder.setReleaseComments(null)
        projectRequestBuilder.setReleasedOn(null)
        projectRequestBuilder.setVersionNickname(null)

        assertFalse(projectRequestBuilder.isValid())

        projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName("FAKE")
        projectRequestBuilder.setProjectLevelAdjustments(false)
        projectRequestBuilder.setProjectOwner("FAKE")
        projectRequestBuilder.setProjectTier(-1)
        projectRequestBuilder.setDescription("FAKE")
        projectRequestBuilder.setVersionName("FAKE")
        projectRequestBuilder.setDistribution("FAKE")
        projectRequestBuilder.setPhase("FAKE")
        projectRequestBuilder.setReleaseComments("FAKE")
        projectRequestBuilder.setReleasedOn("FAKE")
        projectRequestBuilder.setVersionNickname("FAKE")

        assertFalse(projectRequestBuilder.isValid());
    }

    @Test
    public void testNoVersion() {
        String projectName = 'Project'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(projectName)

        assertFalse(projectRequestBuilder.isValid());
    }

    @Test
    public void testNoProject() {
        String versionName = 'Version'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setVersionName(versionName)

        assertFalse(projectRequestBuilder.isValid());
    }

    @Test
    public void testValidValues() {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        final String releasedOn = sdf.format(new Date());

        String projectName = 'Project'
        Boolean projectLevelAdjustments = true
        String projectOwner = 'User'
        Integer projectTier = 2
        String description = 'Project description'
        String versionName = 'Version'
        String distribution = 'inTernal'
        String phase = 'archiVED'
        String releaseComments = 'Comment 1'
        String versionNickname = 'Cobra'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(projectName)
        projectRequestBuilder.setProjectLevelAdjustments(projectLevelAdjustments)
        projectRequestBuilder.setProjectOwner(projectOwner)
        projectRequestBuilder.setProjectTier(projectTier)
        projectRequestBuilder.setDescription(description)
        projectRequestBuilder.setVersionName(versionName)
        projectRequestBuilder.setDistribution(distribution)
        projectRequestBuilder.setPhase(phase)
        projectRequestBuilder.setReleaseComments(releaseComments)
        projectRequestBuilder.setReleasedOn(releasedOn)
        projectRequestBuilder.setVersionNickname(versionNickname)

        assertTrue(projectRequestBuilder.isValid());
    }

    @Test
    public void testConciseValidValues() {
        String projectName = 'Project'
        String versionName = 'Version'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(projectName)
        projectRequestBuilder.setVersionName(versionName)

        assertTrue(projectRequestBuilder.isValid());
    }
}
