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
package com.blackducksoftware.integration.hub.request.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.request.validator.ProjectRequestValidator;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class ProjectRequestBuilder extends AbstractBuilder<ProjectRequest> {
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
        final ProjectRequestValidator validator = new ProjectRequestValidator();
        validator.setProjectName(projectName);
        validator.setDescription(description);
        validator.setProjectLevelAdjustments(projectLevelAdjustments);
        validator.setProjectOwner(projectOwner);
        validator.setProjectTier(projectTier);
        validator.setDistribution(distribution);
        validator.setPhase(phase);
        validator.setVersionName(versionName);
        validator.setVersionNickname(versionNickname);
        validator.setReleaseComments(releaseComments);
        validator.setReleasedOn(releasedOn);
        return validator;
    }

    @Override
    public ProjectRequest buildObject() {
        final ProjectVersionDistributionEnum distributionValue = ProjectVersionDistributionEnum.valueOf(distribution.toUpperCase());
        final ProjectVersionPhaseEnum phaseValue = ProjectVersionPhaseEnum.valueOf(phase.toUpperCase());
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest(distributionValue, phaseValue, versionName);
        projectVersionRequest.setReleaseComments(releaseComments);
        if (StringUtils.isNotBlank(releasedOn)) {
            final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
            try {
                projectVersionRequest.setReleasedOn(sdf.parse(releasedOn));
            } catch (final ParseException e) {

            }
        }
        projectVersionRequest.setNickname(versionNickname);

        final ProjectRequest projectRequest = new ProjectRequest(projectName);
        projectRequest.setDescription(description);
        projectRequest.setProjectLevelAdjustments(projectLevelAdjustments);
        projectRequest.setProjectOwner(projectOwner);
        projectRequest.setProjectTier(projectTier);
        projectRequest.setVersionRequest(projectVersionRequest);
        return projectRequest;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setProjectLevelAdjustments(final Boolean projectLevelAdjustments) {
        this.projectLevelAdjustments = projectLevelAdjustments;
    }

    public void setProjectOwner(final String projectOwner) {
        this.projectOwner = projectOwner;
    }

    public void setProjectTier(final Integer projectTier) {
        this.projectTier = projectTier;
    }

    public void setDistribution(final String distribution) {
        this.distribution = distribution;
    }

    public void setDistribution(final ProjectVersionDistributionEnum distribution) {
        this.distribution = distribution.name();
    }

    public void setPhase(final String phase) {
        this.phase = phase;
    }

    public void setPhase(final ProjectVersionPhaseEnum phase) {
        this.phase = phase.name();
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public void setVersionNickname(final String versionNickname) {
        this.versionNickname = versionNickname;
    }

    public void setReleaseComments(final String releaseComments) {
        this.releaseComments = releaseComments;
    }

    public void setReleasedOn(final String releasedOn) {
        this.releasedOn = releasedOn;
    }
}
