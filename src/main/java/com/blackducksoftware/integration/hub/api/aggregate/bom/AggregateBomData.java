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
package com.blackducksoftware.integration.hub.api.aggregate.bom;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.view.CountTypeEnum;
import com.blackducksoftware.integration.hub.api.view.RiskCountView;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;

public class AggregateBomData {
    private List<VersionBomComponentView> components;

    private int totalComponents;

    private int vulnerabilityRiskHighCount;

    private int vulnerabilityRiskMediumCount;

    private int vulnerabilityRiskLowCount;

    private int vulnerabilityRiskNoneCount;

    private int licenseRiskHighCount;

    private int licenseRiskMediumCount;

    private int licenseRiskLowCount;

    private int licenseRiskNoneCount;

    private int operationalRiskHighCount;

    private int operationalRiskMediumCount;

    private int operationalRiskLowCount;

    private int operationalRiskNoneCount;

    public void setComponents(final List<VersionBomComponentView> components) {
        this.components = components;

        vulnerabilityRiskHighCount = 0;
        vulnerabilityRiskMediumCount = 0;
        vulnerabilityRiskLowCount = 0;

        licenseRiskHighCount = 0;
        licenseRiskMediumCount = 0;
        licenseRiskLowCount = 0;

        operationalRiskHighCount = 0;
        operationalRiskMediumCount = 0;
        operationalRiskLowCount = 0;

        for (final VersionBomComponentView component : components) {
            if (component != null) {
                if (component.getSecurityRiskProfile() != null && component.getSecurityRiskProfile().getCounts() != null
                        && !component.getSecurityRiskProfile().getCounts().isEmpty()) {
                    for (final RiskCountView count : component.getSecurityRiskProfile().getCounts()) {
                        if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                            vulnerabilityRiskHighCount++;
                        } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                            vulnerabilityRiskMediumCount++;
                        } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                            vulnerabilityRiskLowCount++;
                        }
                    }
                }
                if (component.getLicenseRiskProfile() != null && component.getLicenseRiskProfile().getCounts() != null
                        && !component.getLicenseRiskProfile().getCounts().isEmpty()) {
                    for (final RiskCountView count : component.getLicenseRiskProfile().getCounts()) {
                        if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                            licenseRiskHighCount++;
                        } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                            licenseRiskMediumCount++;
                        } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                            licenseRiskLowCount++;
                        }
                    }
                }
                if (component.getOperationalRiskProfile() != null && component.getOperationalRiskProfile().getCounts() != null
                        && !component.getOperationalRiskProfile().getCounts().isEmpty()) {
                    for (final RiskCountView count : component.getOperationalRiskProfile().getCounts()) {
                        if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                            operationalRiskHighCount++;
                        } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                            operationalRiskMediumCount++;
                        } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                            operationalRiskLowCount++;
                        }
                    }
                }
            }
        }

        totalComponents = components.size();

        vulnerabilityRiskNoneCount = totalComponents - vulnerabilityRiskHighCount - vulnerabilityRiskMediumCount
                - vulnerabilityRiskLowCount;
        licenseRiskNoneCount = totalComponents - licenseRiskHighCount - licenseRiskMediumCount - licenseRiskLowCount;
        operationalRiskNoneCount = totalComponents - operationalRiskHighCount - operationalRiskMediumCount
                - operationalRiskLowCount;
    }

    public double getPercentage(final double count) {
        final double totalCount = totalComponents;
        double percentage = 0;
        if (totalCount > 0 && count > 0) {
            percentage = (count / totalCount) * 100;
        }
        return percentage;
    }

    public String htmlEscape(final String valueToEscape) {
        if (StringUtils.isBlank(valueToEscape)) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(valueToEscape);
    }

    public List<VersionBomComponentView> getComponents() {
        return components;
    }

    public int getVulnerabilityRiskHighCount() {
        return vulnerabilityRiskHighCount;
    }

    public int getVulnerabilityRiskMediumCount() {
        return vulnerabilityRiskMediumCount;
    }

    public int getVulnerabilityRiskLowCount() {
        return vulnerabilityRiskLowCount;
    }

    public int getVulnerabilityRiskNoneCount() {
        return vulnerabilityRiskNoneCount;
    }

    public int getLicenseRiskHighCount() {
        return licenseRiskHighCount;
    }

    public int getLicenseRiskMediumCount() {
        return licenseRiskMediumCount;
    }

    public int getLicenseRiskLowCount() {
        return licenseRiskLowCount;
    }

    public int getLicenseRiskNoneCount() {
        return licenseRiskNoneCount;
    }

    public int getOperationalRiskHighCount() {
        return operationalRiskHighCount;
    }

    public int getOperationalRiskMediumCount() {
        return operationalRiskMediumCount;
    }

    public int getOperationalRiskLowCount() {
        return operationalRiskLowCount;
    }

    public int getOperationalRiskNoneCount() {
        return operationalRiskNoneCount;
    }

}
