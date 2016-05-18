/*******************************************************************************
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

/**
 * Detailed release summary.
 *
 */
public class DetailedReleaseSummary {
	private final String projectId;

	private final String versionId;

	private final String projectName;

	private final String version;

	private final String versionComments;

	private final String nickname;

	private final String releasedOn;

	private final String phase;

	private final String distribution;

	private final URLProvider uiUrlGenerator;

	public DetailedReleaseSummary(final String projectId,
			final String versionId,
			final String projectName,
			final String version,
			final String versionComments,
			final String nickname,
			final String releasedOn,
			final String phase,
			final String distribution, final URLProvider uiUrlGenerator) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.projectName = projectName;
		this.version = version;
		this.versionComments = versionComments;
		this.nickname = nickname;
		this.releasedOn = releasedOn;
		this.phase = phase;
		this.distribution = distribution;
		this.uiUrlGenerator = uiUrlGenerator;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getVersionId() {
		return versionId;
	}

	public UUID getProjectUUId() {
		if (StringUtils.isBlank(projectId)) {
			return null;
		}
		try {
			return UUID.fromString(projectId);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public UUID getVersionUUId() {
		if (StringUtils.isBlank(versionId)) {
			return null;
		}
		try {
			return UUID.fromString(versionId);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public String getProjectName() {
		return projectName;
	}

	public String getVersion() {
		return version;
	}

	public String getVersionComments() {
		return versionComments;
	}

	public String getNickname() {
		return nickname;
	}

	public String getReleasedOn() {
		return releasedOn;
	}

	public String getPhase() {
		return phase;
	}

	public String getDistribution() {
		return distribution;
	}

	public String getPhaseDisplayValue() {
		if (StringUtils.isBlank(phase)) {
			return null;
		}
		return PhaseEnum.getPhaseEnum(phase).getDisplayValue();
	}

	public String getDistributionDisplayValue() {
		if (StringUtils.isBlank(distribution)) {
			return null;
		}
		return DistributionEnum.getDistributionEnum(distribution).getDisplayValue();
	}

	public DateTime getReleasedOnTime() {
		if (StringUtils.isBlank(releasedOn)) {
			return null;
		}
		try {
			return new DateTime(releasedOn);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public URLProvider getUiUrlGenerator() {
		return uiUrlGenerator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + ((releasedOn == null) ? 0 : releasedOn.hashCode());
		result = prime * result + ((uiUrlGenerator == null) ? 0 : uiUrlGenerator.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((versionComments == null) ? 0 : versionComments.hashCode());
		result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
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
		if (!(obj instanceof DetailedReleaseSummary)) {
			return false;
		}
		final DetailedReleaseSummary other = (DetailedReleaseSummary) obj;
		if (distribution == null) {
			if (other.distribution != null) {
				return false;
			}
		} else if (!distribution.equals(other.distribution)) {
			return false;
		}
		if (nickname == null) {
			if (other.nickname != null) {
				return false;
			}
		} else if (!nickname.equals(other.nickname)) {
			return false;
		}
		if (phase == null) {
			if (other.phase != null) {
				return false;
			}
		} else if (!phase.equals(other.phase)) {
			return false;
		}
		if (projectId == null) {
			if (other.projectId != null) {
				return false;
			}
		} else if (!projectId.equals(other.projectId)) {
			return false;
		}
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (releasedOn == null) {
			if (other.releasedOn != null) {
				return false;
			}
		} else if (!releasedOn.equals(other.releasedOn)) {
			return false;
		}
		if (uiUrlGenerator == null) {
			if (other.uiUrlGenerator != null) {
				return false;
			}
		} else if (!uiUrlGenerator.equals(other.uiUrlGenerator)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		if (versionComments == null) {
			if (other.versionComments != null) {
				return false;
			}
		} else if (!versionComments.equals(other.versionComments)) {
			return false;
		}
		if (versionId == null) {
			if (other.versionId != null) {
				return false;
			}
		} else if (!versionId.equals(other.versionId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DetailedReleaseSummary [projectId=");
		builder.append(projectId);
		builder.append(", versionId=");
		builder.append(versionId);
		builder.append(", projectName=");
		builder.append(projectName);
		builder.append(", version=");
		builder.append(version);
		builder.append(", versionComments=");
		builder.append(versionComments);
		builder.append(", nickname=");
		builder.append(nickname);
		builder.append(", releasedOn=");
		builder.append(releasedOn);
		builder.append(", phase=");
		builder.append(phase);
		builder.append(", distribution=");
		builder.append(distribution);
		builder.append(", uiUrlGenerator=");
		builder.append(uiUrlGenerator);
		builder.append("]");
		return builder.toString();
	}

	static public class URLProvider {
		private final String baseUrl;

		public URLProvider(final String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
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
			if (!(obj instanceof URLProvider)) {
				return false;
			}
			final URLProvider other = (URLProvider) obj;
			if (baseUrl == null) {
				if (other.baseUrl != null) {
					return false;
				}
			} else if (!baseUrl.equals(other.baseUrl)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("URLProvider [baseUrl=");
			builder.append(baseUrl);
			builder.append("]");
			return builder.toString();
		}

	}

}
