/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

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
			// List<DetailedCodeLocation> detailedCodeLocations,
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
		if (StringUtils.isBlank(getBaseUrl()) || entry == null ||
				entry.getProducerProject() == null || StringUtils.isBlank(entry.getProducerProject().getId())) {
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
		if (StringUtils.isBlank(getBaseUrl()) || entry == null ||
				entry.getProducerReleases() == null || StringUtils.isBlank(entry.getProducerReleasesId())) {
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
