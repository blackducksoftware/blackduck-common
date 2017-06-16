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
package com.blackducksoftware.integration.hub.request.validator

import java.text.SimpleDateFormat

import org.apache.commons.lang3.StringUtils

import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum
import com.blackducksoftware.integration.hub.request.ProjectRequestField
import com.blackducksoftware.integration.hub.request.ProjectVersionRequestField
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.validator.AbstractValidator
import com.blackducksoftware.integration.validator.ValidationResult
import com.blackducksoftware.integration.validator.ValidationResultEnum
import com.blackducksoftware.integration.validator.ValidationResults

class ProjectRequestValidator extends AbstractValidator {
    private String projectName

    private String description

    private Boolean projectLevelAdjustments

    private String projectOwner

    private Integer projectTier

    private String distribution

    private String phase

    private String versionName

    private String versionNickname

    private String releaseComments

    private String releasedOn

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults()
        validateProject(result)
        validateVersion(result)
        return result
    }

    public void validateProject(ValidationResults result){
        if (StringUtils.isBlank(projectName)) {
            result.addResult(ProjectRequestField.NAME, new ValidationResult(ValidationResultEnum.ERROR, 'Did not provide a project name.'))
        }
    }

    public void validateVersion(ValidationResults result){
        if (StringUtils.isBlank(versionName)) {
            result.addResult(ProjectVersionRequestField.VERSIONNAME, new ValidationResult(ValidationResultEnum.ERROR, 'Did not provide a version name.'))
        }
        if (StringUtils.isBlank(distribution)) {
            result.addResult(ProjectVersionRequestField.DISTRIBUTION, new ValidationResult(ValidationResultEnum.ERROR, 'Did not provide a version distribution.'))
        } else {
            try {
                ProjectVersionDistributionEnum.valueOf(distribution?.toUpperCase())
            } catch(Exception e){
                result.addResult(ProjectVersionRequestField.DISTRIBUTION, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e))
            }
        }

        if (StringUtils.isBlank(phase)) {
            result.addResult(ProjectVersionRequestField.PHASE, new ValidationResult(ValidationResultEnum.ERROR, 'Did not provide a version phase.'))
        } else {
            try {
                ProjectVersionPhaseEnum.valueOf(phase?.toUpperCase())
            } catch(Exception e){
                result.addResult(ProjectVersionRequestField.PHASE, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e))
            }
        }

        if (StringUtils.isNotBlank(releasedOn)) {
            try{
                final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT)
                sdf.parse(releasedOn)
            } catch (Exception e) {
                result.addResult(ProjectVersionRequestField.RELEASEDON, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e))
            }
        }
    }


    public void setProjectName(String projectName) {
        this.projectName = projectName
    }

    public void setDescription(String description) {
        this.description = description
    }

    public void setProjectLevelAdjustments(Boolean projectLevelAdjustments) {
        this.projectLevelAdjustments = projectLevelAdjustments
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner
    }

    public void setProjectTier(Integer projectTier) {
        this.projectTier = projectTier
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution
    }

    public void setPhase(String phase) {
        this.phase = phase
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName
    }

    public void setVersionNickname(String versionNickname) {
        this.versionNickname = versionNickname
    }

    public void setReleaseComments(String releaseComments) {
        this.releaseComments = releaseComments
    }

    public void setReleasedOn(String releasedOn) {
        this.releasedOn = releasedOn
    }
}
