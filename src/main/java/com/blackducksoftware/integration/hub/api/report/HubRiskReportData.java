/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.api.report;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class HubRiskReportData {
    private VersionReport report;

    private int totalBomEntries;

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

    public void setReport(final VersionReport report) {
        this.report = report;

        vulnerabilityRiskHighCount = 0;
        vulnerabilityRiskMediumCount = 0;
        vulnerabilityRiskLowCount = 0;

        licenseRiskHighCount = 0;
        licenseRiskMediumCount = 0;
        licenseRiskLowCount = 0;

        operationalRiskHighCount = 0;
        operationalRiskMediumCount = 0;
        operationalRiskLowCount = 0;

        final List<AggregateBomViewEntry> bomEntries = report.getAggregateBomViewEntries();
        for (final AggregateBomViewEntry bomEntry : bomEntries) {
            if (bomEntry != null) {
                if (bomEntry.getVulnerabilityRisk() != null) {
                    if (bomEntry.getVulnerabilityRisk().getHIGH() > 0) {
                        vulnerabilityRiskHighCount++;
                    } else if (bomEntry.getVulnerabilityRisk().getMEDIUM() > 0) {
                        vulnerabilityRiskMediumCount++;
                    } else if (bomEntry.getVulnerabilityRisk().getLOW() > 0) {
                        vulnerabilityRiskLowCount++;
                    }
                }
                if (bomEntry.getLicenseRisk() != null) {
                    if (bomEntry.getLicenseRisk().getHIGH() > 0) {
                        licenseRiskHighCount++;
                    } else if (bomEntry.getLicenseRisk().getMEDIUM() > 0) {
                        licenseRiskMediumCount++;
                    } else if (bomEntry.getLicenseRisk().getLOW() > 0) {
                        licenseRiskLowCount += 1;
                    }
                }
                if (bomEntry.getOperationalRisk() != null) {
                    if (bomEntry.getOperationalRisk().getHIGH() > 0) {
                        operationalRiskHighCount++;
                    } else if (bomEntry.getOperationalRisk().getMEDIUM() > 0) {
                        operationalRiskMediumCount++;
                    } else if (bomEntry.getOperationalRisk().getLOW() > 0) {
                        operationalRiskLowCount++;
                    }
                }
            }
        }

        totalBomEntries = bomEntries.size();

        vulnerabilityRiskNoneCount = totalBomEntries - vulnerabilityRiskHighCount - vulnerabilityRiskMediumCount
                - vulnerabilityRiskLowCount;
        licenseRiskNoneCount = totalBomEntries - licenseRiskHighCount - licenseRiskMediumCount - licenseRiskLowCount;
        operationalRiskNoneCount = totalBomEntries - operationalRiskHighCount - operationalRiskMediumCount
                - operationalRiskLowCount;
    }

    public double getPercentage(final double count) {
        final double totalCount = totalBomEntries;
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

    public List<AggregateBomViewEntry> getBomEntries() {
        return report.getAggregateBomViewEntries();
    }

    public VersionReport getReport() {
        return report;
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
