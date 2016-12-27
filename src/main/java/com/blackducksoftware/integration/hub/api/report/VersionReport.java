/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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

import org.apache.commons.lang3.StringUtils;

/**
 * Version report.
 *
 */
public class VersionReport {
    private final DetailedReleaseSummary detailedReleaseSummary;

    private final List<AggregateBomViewEntry> aggregateBomViewEntries;

    public VersionReport(final DetailedReleaseSummary detailedReleaseSummary,
            final List<AggregateBomViewEntry> aggregateBomViewEntries) {
        this.detailedReleaseSummary = detailedReleaseSummary;
        this.aggregateBomViewEntries = aggregateBomViewEntries;
    }

    public DetailedReleaseSummary getDetailedReleaseSummary() {
        return detailedReleaseSummary;
    }

    public String getBaseUrl() {
        if (detailedReleaseSummary == null || detailedReleaseSummary.getUiUrlGenerator() == null) {
            return null;
        }
        return detailedReleaseSummary.getUiUrlGenerator().getBaseUrl();
    }

    public String getReportProjectUrl() {
        if (detailedReleaseSummary == null || StringUtils.isBlank(getBaseUrl())
                || StringUtils.isBlank(detailedReleaseSummary.getProjectId())) {
            return null;
        }

        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("projects/id:");
        urlBuilder.append(detailedReleaseSummary.getProjectId());

        return urlBuilder.toString();
    }

    public String getReportVersionUrl() {
        if (detailedReleaseSummary == null || StringUtils.isBlank(getBaseUrl())
                || StringUtils.isBlank(detailedReleaseSummary.getVersionId())) {
            return null;
        }

        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("versions/id:");
        urlBuilder.append(detailedReleaseSummary.getVersionId());
        urlBuilder.append("/view:bom");

        return urlBuilder.toString();
    }

    public String getComponentUrl(final AggregateBomViewEntry entry) {
        if (StringUtils.isBlank(getBaseUrl()) || entry == null || entry.getProducerProject() == null
                || StringUtils.isBlank(entry.getProducerProject().getId())) {
            return null;
        }

        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("projects/id:");
        urlBuilder.append(entry.getProducerProject().getId());

        return urlBuilder.toString();
    }

    public String getVersionUrl(final AggregateBomViewEntry entry) {
        if (StringUtils.isBlank(getBaseUrl()) || entry == null || entry.getProducerReleases() == null
                || StringUtils.isBlank(entry.getProducerReleasesId())) {
            return null;
        }

        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("versions/id:");
        urlBuilder.append(entry.getProducerReleasesId());

        return urlBuilder.toString();
    }

    public List<AggregateBomViewEntry> getAggregateBomViewEntries() {
        return aggregateBomViewEntries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateBomViewEntries == null) ? 0 : aggregateBomViewEntries.hashCode());
        result = prime * result + ((detailedReleaseSummary == null) ? 0 : detailedReleaseSummary.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VersionReport)) {
            return false;
        }
        final VersionReport other = (VersionReport) obj;
        if (aggregateBomViewEntries == null) {
            if (other.aggregateBomViewEntries != null) {
                return false;
            }
        } else if (!aggregateBomViewEntries.equals(other.aggregateBomViewEntries)) {
            return false;
        }
        if (detailedReleaseSummary == null) {
            if (other.detailedReleaseSummary != null) {
                return false;
            }
        } else if (!detailedReleaseSummary.equals(other.detailedReleaseSummary)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("VersionReport [detailedReleaseSummary=");
        builder.append(detailedReleaseSummary);
        builder.append(", aggregateBomViewEntries=");
        builder.append(aggregateBomViewEntries);
        builder.append("]");
        return builder.toString();
    }

}
