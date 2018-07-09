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
package com.blackducksoftware.integration.hub.service.model;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class ProjectRequestValidator extends AbstractValidator {
    private final String projectName;
    private final String distribution;
    private final String phase;
    private final String versionName;
    private final String releasedOn;

    public ProjectRequestValidator(final ProjectRequestBuilder projectRequestBuilder) {
        this.projectName = projectRequestBuilder.getProjectName();
        this.distribution = projectRequestBuilder.getDistribution();
        this.phase = projectRequestBuilder.getPhase();
        this.versionName = projectRequestBuilder.getVersionName();
        this.releasedOn = projectRequestBuilder.getReleasedOn();
    }

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults();
        validateProject(result);
        validateVersion(result);
        return result;
    }

    public void validateProject(final ValidationResults result) {
        if (StringUtils.isBlank(projectName)) {
            result.addResult(ProjectRequestField.NAME, new ValidationResult(ValidationResultEnum.ERROR, "Did not provide a project name."));
        }
    }

    public void validateVersion(final ValidationResults result) {
        if (StringUtils.isBlank(versionName)) {
            result.addResult(ProjectVersionRequestField.VERSIONNAME, new ValidationResult(ValidationResultEnum.ERROR, "Did not provide a version name."));
        }
        if (StringUtils.isBlank(distribution)) {
            result.addResult(ProjectVersionRequestField.DISTRIBUTION, new ValidationResult(ValidationResultEnum.ERROR, "Did not provide a version distribution."));
        } else {
            try {
                ProjectVersionDistributionType.valueOf(distribution.toUpperCase());
            } catch (final Exception e) {
                result.addResult(ProjectVersionRequestField.DISTRIBUTION, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            }
        }

        if (StringUtils.isBlank(phase)) {
            result.addResult(ProjectVersionRequestField.PHASE, new ValidationResult(ValidationResultEnum.ERROR, "Did not provide a version phase."));
        } else {
            try {
                ProjectVersionPhaseType.valueOf(phase.toUpperCase());
            } catch (final Exception e) {
                result.addResult(ProjectVersionRequestField.PHASE, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            }
        }

        if (StringUtils.isNotBlank(releasedOn)) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
                sdf.parse(releasedOn);
            } catch (final Exception e) {
                result.addResult(ProjectVersionRequestField.RELEASEDON, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            }
        }
    }

}
