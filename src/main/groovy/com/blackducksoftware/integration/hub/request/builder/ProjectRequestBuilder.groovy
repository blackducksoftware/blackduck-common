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
package com.blackducksoftware.integration.hub.request.builder

import java.text.SimpleDateFormat

import com.blackducksoftware.integration.builder.AbstractBuilder
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum
import com.blackducksoftware.integration.hub.model.request.ProjectRequest
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest
import com.blackducksoftware.integration.hub.request.validator.ProjectRequestValidator
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.validator.AbstractValidator

class ProjectRequestBuilder extends AbstractBuilder<ProjectRequest> {
    private String projectName;

    private String description;

    private Boolean projectLevelAdjustments;

    private String projectOwner;

    private Integer projectTier;

    private String distribution = ProjectVersionDistributionEnum.EXTERNAL.name();

    private String phase = ProjectVersionPhaseEnum.DEVELOPMENT.name();

    private String versionName;

    private String versionNickname;

    private String releaseComments;

    private String releasedOn;

    @Override
    public AbstractValidator createValidator() {
        ProjectRequestValidator validator = new ProjectRequestValidator()
        validator.setProjectName(projectName)
        validator.setDescription(description)
        validator.setProjectLevelAdjustments(projectLevelAdjustments)
        validator.setProjectOwner(projectOwner)
        validator.setProjectTier(projectTier)
        validator.setDistribution(distribution)
        validator.setPhase(phase)
        validator.setVersionName(versionName)
        validator.setVersionNickname(versionNickname)
        validator.setReleaseComments(releaseComments)
        validator.setReleasedOn(releasedOn)
        return validator;
    }

    @Override
    public ProjectRequest buildObject() {
        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest(ProjectVersionDistributionEnum.valueOf(distribution?.toUpperCase()), ProjectVersionPhaseEnum.valueOf(phase?.toUpperCase()), versionName)
        projectVersionRequest.setReleaseComments(releaseComments)
        if (releasedOn){
            final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT)
            projectVersionRequest.setReleasedOn(sdf.parse(releasedOn))
        }
        projectVersionRequest.setNickname(versionNickname)

        ProjectRequest projectRequest = new ProjectRequest(projectName)
        projectRequest.setDescription(description)
        projectRequest.setProjectLevelAdjustments(projectLevelAdjustments)
        projectRequest.setProjectOwner(projectOwner)
        projectRequest.setProjectTier(projectTier)
        projectRequest.setVersionRequest(projectVersionRequest)
        return projectRequest;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProjectLevelAdjustments(Boolean projectLevelAdjustments) {
        this.projectLevelAdjustments = projectLevelAdjustments;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    public void setProjectTier(Integer projectTier) {
        this.projectTier = projectTier;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public void setDistribution(ProjectVersionDistributionEnum distribution) {
        this.distribution = distribution.name();
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public void setPhase(ProjectVersionPhaseEnum phase) {
        this.phase = phase.name();
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionNickname(String versionNickname) {
        this.versionNickname = versionNickname;
    }

    public void setReleaseComments(String releaseComments) {
        this.releaseComments = releaseComments;
    }

    public void setReleasedOn(String releasedOn) {
        this.releasedOn = releasedOn;
    }
}
